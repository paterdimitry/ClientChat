package client.models;

import client.controllers.ChangePasswordController;
import client.controllers.ChangeUsernameController;
import client.controllers.ChatController;
import client.views.ChatClient;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {

    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + pass
    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error message
    private static final String CLIENT_MSG_CMD_PREFIX = "/cmsg"; // + msg
    public static final String PRIVATE_MSG_CMD_PREFIX = "/w"; //recipient + msg
    private static final String SERVER_MSG_CMD_PREFIX = "/serverMsg"; // + msg
    private static final String END_CMD_PREFIX = "/end"; //
    private static final String USER_LIST_CMD = "/usrlst";

    private static final String UPDATE_USERNAME_PREFIX = "/updusrname";
    private static final String UPDATE_USERNAME_OK_PREFIX = "/updusrnameok";
    private static final String UPDATE_USERNAME_ERR_PREFIX = "/updusrnameerr";

    private static final String REG_PREFIX = "/reg";
    private static final String REGERR_PREFIX = "/regerr";
    private static final String REGOK_PREFIX = "/regok";

    private static final String UPDATE_PASSWORD_PREFIX = "/updpswrd"; // + username + password + newPassword
    private static final String UPDATE_PASSWORD_OK_PREFIX = "/updpswrdok"; //
    private static final String UPDATE_PASSWORD_ERR_PREFIX = "/updpswrderr"; // + message

    private static final String DEFAULT_SERVER_HOST = "localhost";
    private static final int DEFAULT_SERVER_PORT = 8888;


    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final String host;
    private final int port;
    private String username;

    public Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Network() {
        this.host = DEFAULT_SERVER_HOST;
        this.port = DEFAULT_SERVER_PORT;
    }

    public void connect() {
        try {
            socket = new Socket(host, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            connectionAlert();
        }
    }

    //метод отправки сообщения. вызывается из контроллера
    public void sendMessage(String message) {
        try {
            out.writeUTF(String.format("%s %s", CLIENT_MSG_CMD_PREFIX, message));
        } catch (IOException e) {
            messageAlert();
        }
    }

    public void sendPrivateMessage(String message) {
        try {
            out.writeUTF(String.format("%s", message));
        } catch (IOException e) {
            messageAlert();
        }
    }

    //ожидание сообщения в непрерывном потоке
    public void waitMessage(ChatController controller) {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    String inMessage = in.readUTF();
                    String pref = inMessage.split("\\s+", 2)[0];
                    String message = inMessage.split("\\s+", 2)[1];
                    //в платформе через switch определяем формат вывода сообщения или действие по префиксу
                    Platform.runLater(() -> {
                        switch (pref) {
                            case CLIENT_MSG_CMD_PREFIX -> controller.sendMessageToList(message);
                            case SERVER_MSG_CMD_PREFIX -> controller.sendMessageToList(String.format("<<< %s >>>", message));
                            case PRIVATE_MSG_CMD_PREFIX -> controller.sendMessageToList(String.format("%S", message));
                            case USER_LIST_CMD -> refreshUserList(controller, message);
                            case END_CMD_PREFIX -> closeServer(controller);
                            case UPDATE_USERNAME_OK_PREFIX -> {
                                username = message;
                                ChangeUsernameController.okUpdateUsername(message);
                                ChatClient.changeStageTitle();
                            }
                            case UPDATE_USERNAME_ERR_PREFIX -> ChangeUsernameController.errorUpdateUsername(message);
                            case UPDATE_PASSWORD_OK_PREFIX -> ChangePasswordController.okUpdatePassword();
                            case UPDATE_PASSWORD_ERR_PREFIX -> ChangePasswordController.errorUpdatePassword(message);
                        }
                    });
                }
            } catch (IOException e) {
                connectionAlert();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    //Обновление списка пользователей

    private void refreshUserList(ChatController controller, String message) {
        controller.resetUserList(); //очищаем список
        String[] users = message.split("\\s+");
        for (String user : users) {
            controller.addClientToList(user); //и заполняем заново
        }
    }

    public String sendAuthMessage(String login, String password) {
        try {
            out.writeUTF(String.format("%s %s %s", AUTH_CMD_PREFIX, login, password));
            String response = in.readUTF();
            if (response.startsWith(AUTHOK_CMD_PREFIX)) {
                this.username = response.split("\\s+", 2)[1];
                return null;
            } else {
                return response.split("\\s+", 2)[1];
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public String sendRegMessage(String login, String password, String username) {
        try {
            out.writeUTF(String.format("%s %s %s %s", REG_PREFIX, login, password, username));
            String response = in.readUTF();
            if (response.startsWith(REGOK_PREFIX))
                return null;
            if (response.startsWith(REGERR_PREFIX))
                return response.split("\\s+", 2)[1];
            return "Неизвестная ошибка";
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public String getUsername() {
        return username;
    }

    private void closeServer(ChatController controller) {
        controller.sendMessageToList("<<< Сервер остановлен! Приложение закрывается >>>");
        try {

            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private void messageAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setContentText("Ошибка отправки сообщения");
        alert.show();
    }

    private void connectionAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка соединения");
        alert.setContentText("Соединение прервано");
        alert.show();
    }

    public void sendUpdateUsername(String newUsername, String password) {
        try {
            out.writeUTF(String.format("%s %s %s %s", UPDATE_USERNAME_PREFIX, username, password, newUsername));
        } catch (IOException e) {
            ChangeUsernameController.errorUpdateUsername("Ошибка смены имени");
        }
    }

    public void sendUpdatePassword(String password, String newPassword) {
        try {
            out.writeUTF(String.format("%s %s %s %s",UPDATE_PASSWORD_PREFIX, username, password, newPassword));
        } catch (IOException e) {
            ChangePasswordController.errorUpdatePassword("Ошибка смены пароля");
        }
    }
}
