package server.chat.handler;

import server.chat.MyServer;
import server.chat.service.interfaces.AuthService;
import server.chat.service.interfaces.ChangePasswordService;
import server.chat.service.interfaces.ChangeUsernameService;
import server.chat.service.interfaces.RegService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientHandler {
    private final MyServer myServer;
    private final Socket clientSocket;
    DataInputStream in;
    DataOutputStream out;
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + pass
    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error message
    private static final String CLIENT_MSG_CMD_PREFIX = "/cmsg"; // + msg
    private static final String PRIVATE_MSG_CMD_PREFIX = "/w"; //recipient + msg
    private static final String SERVER_MSG_CMD_PREFIX = "/serverMsg"; // + msg
    private static final String END_CMD_PREFIX = "/end"; //
    private static final String USER_LIST_CMD_PREFIX = "/usrlst";

    private static final String UPDATE_USERNAME_PREFIX = "/updusrname";
    private static final String UPDATE_USERNAME_OK_PREFIX = "/updusrnameok";
    private static final String UPDATE_USERNAME_ERR_PREFIX = "/updusrnameerr";

    private static final String REG_PREFIX = "/reg"; // + login + password + username
    private static final String REGERR_PREFIX = "/regerr"; // + error message
    private static final String REGOK_PREFIX = "/regok"; //

    private static final String UPDATE_PASSWORD_PREFIX = "/updpswrd"; // + username + password + newPassword
    private static final String UPDATE_PASSWORD_OK_PREFIX = "/updpswrdok"; //
    private static final String UPDATE_PASSWORD_ERR_PREFIX = "/updpswrderr"; // + message

    private String username;

    public ClientHandler(MyServer myServer, Socket socket) {
        this.myServer = myServer;
        this.clientSocket = socket;
    }

    public void handle() throws IOException {
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());

        new Thread(() -> {
            try {
                waitStartMessage();
                readMessage();
            } catch (IOException e) {
                myServer.unsubscribe(this);
            }
        }).start();
    }

    private void waitStartMessage() throws IOException {
        while (true) {
            String message = in.readUTF();
            if (message.startsWith(AUTH_CMD_PREFIX)) {
                boolean isAuthSuccess = processAuthCommand(message);
                if (isAuthSuccess)
                    break;
                else
                    out.writeUTF(AUTHERR_CMD_PREFIX + " Ошибка авторизации!");
            } else if (message.startsWith(REG_PREFIX)) {
                processRegCommand(message);

            } else {
                out.writeUTF(REGERR_PREFIX + " Ошибка регистрации!");
            }
        }
    }

    private void processRegCommand(String message) throws IOException {
        String[] parts = message.split("\\s+", 4);
        String login = parts[1];
        String password = parts[2];
        String username = parts[3];

        RegService regService = myServer.getRegService();
        try {
            if (!regService.isLoginEmpty(login)) {
                out.writeUTF(REGERR_PREFIX + " Указанный login уже используется");
                return;
            } else if (!regService.isUsernameEmpty(username)) {
                out.writeUTF(REGERR_PREFIX + " Указанный username уже используется");
            } else if (regService.registration(login, password, username)) {
                out.writeUTF(REGOK_PREFIX);
                return;
            } else {
                out.writeUTF(REGERR_PREFIX + " Ошибка решистрации");
                return;
            }
        } catch (SQLException e) {
            out.writeUTF(REGERR_PREFIX + " Ошибка решистрации");
            return;
        }
    }

    private boolean processAuthCommand(String message) throws IOException {
        String[] parts = message.split("\\s+", 3);
        String login = parts[1];
        String password = parts[2];

        AuthService authService = myServer.getAuthService();

        try {
            username = authService.getUsernameByLogin(login, password);
            if (username != null) {
                if (myServer.isUsernameBusy(username)) {
                    out.writeUTF(AUTHERR_CMD_PREFIX + " Пользователь с таким именем уже подключен");
                    return false;
                } else {
                    out.writeUTF(String.format("%s %s", AUTHOK_CMD_PREFIX, username));
                    myServer.subscribe(this);
                    return true;
                }
            } else {
                out.writeUTF(AUTHERR_CMD_PREFIX + " Неверные логин или пароль!");
                return false;
            }
        } catch (SQLException e) {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Ошибка авторизации");
            return false;
        }
    }

    private void readMessage() throws IOException {
        while (true) {
            String message = in.readUTF();
            String[] parts = message.split("\\s+", 2); //отделеям префикс
            String pref = parts[0];
            message = parts[1];
            switch (pref) {
                case CLIENT_MSG_CMD_PREFIX -> myServer.broadcastMessage(this, message);
                case PRIVATE_MSG_CMD_PREFIX -> {
                    String[] division = message.split("\\s+", 2); //отделяем адресата
                    try {
                        myServer.broadcastMessage(this, division[0], division[1]); //передаем адресата и сообщение
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("Недопустимый формат сообщения");
                    }
                }
                case UPDATE_USERNAME_PREFIX -> updateUsername(message);
                case UPDATE_PASSWORD_PREFIX -> updatePassword(message);
            }
        }
    }


    private void updateUsername(String message) throws IOException {
        String lastUsername = message.split("\\s+", 3)[0];
        String password = message.split("\\s+", 3)[1];
        String newUsername = message.split("\\s+", 3)[2];
        ChangeUsernameService changeUsernameService = myServer.getChangeUsernameService();
        if (lastUsername.equals(newUsername)) {
            out.writeUTF(UPDATE_USERNAME_ERR_PREFIX + " Введенный username не отличается от Вашего");
            return;
        }
        try {
            if (changeUsernameService.isUsernameEmpty(newUsername)) {
                newUsername = changeUsernameService.updateUsername(lastUsername, password, newUsername);
                if (newUsername != null) {
                    username = newUsername;
                    myServer.broadcastUpdateUsernameMessage(this, lastUsername, newUsername);
                    myServer.broadcastUserList();
                    out.writeUTF(String.format("%s %s", UPDATE_USERNAME_OK_PREFIX, username));
                } else {
                    out.writeUTF(UPDATE_USERNAME_ERR_PREFIX + " Введен неверный пароль");
                }
            } else {
                out.writeUTF(UPDATE_USERNAME_ERR_PREFIX + " Введенный username уже используется");
            }
        } catch (SQLException e) {
            out.writeUTF(UPDATE_USERNAME_ERR_PREFIX + " Ошибка смены username");
        }
    }

    private void updatePassword(String message) throws IOException {
        String username = message.split("\\s+", 3)[0];
        String password = message.split("\\s+", 3)[1];
        String newPassword = message.split("\\s+", 3)[2];
        ChangePasswordService changePasswordService = myServer.getChangePasswordService();
        try {
            if (password.equals(changePasswordService.getPassword(username))) {
                if (password.equals(newPassword)) {
                    out.writeUTF(UPDATE_PASSWORD_ERR_PREFIX + " Введенный пароль не отличается от Вашего");
                    return;
                }
                if (changePasswordService.updatePassword(username, password, newPassword))
                    out.writeUTF(UPDATE_PASSWORD_OK_PREFIX + " Пароль изменен");
            } else
                out.writeUTF(UPDATE_PASSWORD_ERR_PREFIX + " Введен неверный пароль");
        } catch (SQLException e) {
            out.writeUTF(UPDATE_PASSWORD_ERR_PREFIX + " Ошибка смены пароля");
        }
    }

    public void sendServerMessage(String message) throws IOException {
        out.writeUTF(String.format("%s %s", SERVER_MSG_CMD_PREFIX, message));
    }

    public void sendMessage(String message) throws IOException {
        out.writeUTF(String.format("%s %s Я: %s", CLIENT_MSG_CMD_PREFIX, dateFormat.format(new Date()), message));
    }

    public void sendMessage(String sender, String message) throws IOException {
        out.writeUTF(String.format("%s %s %s: %s", CLIENT_MSG_CMD_PREFIX, dateFormat.format(new Date()), sender, message));
    }

    public void sendPrivateMessage() throws IOException {
        out.writeUTF("Адресат не найден");
    }

    public void sendPrivateMessage(String recipient, String message) throws IOException {
        out.writeUTF(String.format("%s %s Я отправил лично %s: %s", PRIVATE_MSG_CMD_PREFIX, dateFormat.format(new Date()), recipient, message));
    }

    public void sendPrivateMessage(String sender, String recipient, String message) throws IOException {
        out.writeUTF(String.format("%s %s %s отправил вам личное сообщение: %s", PRIVATE_MSG_CMD_PREFIX, dateFormat.format(new Date()), sender, message));
    }

    public String getUsername() {
        return username;
    }

    public void sendUserList(String userList) throws IOException {
        out.writeUTF(String.format("%s %s", USER_LIST_CMD_PREFIX, userList));
    }

    public void sendStopServerMessage() throws IOException {
        out.writeUTF(END_CMD_PREFIX);
    }
}
