package server.chat.handler;

import server.chat.MyServer;
import server.chat.auth.AuthService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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
    private static final String USER_LIST_CMD = "/usrlst";
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
                authentication();
                readMessage();
            } catch (IOException e) {
                myServer.unsubscribe(this);
            }
        }).start();
    }

    private void readMessage() throws IOException {
        while (true) {
            String message = in.readUTF();
            String[] parts = message.split("\\s+", 2); //отделеям префикс
            String pref = parts[0];
            message = parts[1];
            if (pref.equals(CLIENT_MSG_CMD_PREFIX)) {
                myServer.broadcastMessage(this, message);
            }
            if (pref.equals(PRIVATE_MSG_CMD_PREFIX)) {
                String[] division = message.split("\\s+", 2); //отделяем адресата
                try {
                    myServer.broadcastMessage(this, division[0], division[1]); //передаем адресата и сообщение
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Недопустимый формат сообщения");
                }
            }
        }
    }

    private void authentication() throws IOException {
        while (true) {
            String message = in.readUTF();
            if (message.startsWith(AUTH_CMD_PREFIX)) {
                boolean isAuthSuccess = processAuthCommand(message);
                if (isAuthSuccess)
                    break;
            } else {
                out.writeUTF(AUTHERR_CMD_PREFIX + "Ошибка авторизации!");
            }
        }
    }

    private boolean processAuthCommand(String message) throws IOException {
        String[] parts = message.split("\\s+", 3);
        String login = parts[1];
        String password = parts[2];

        AuthService authService = myServer.getAuthService();

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
        out.writeUTF(String.format("%s %s", USER_LIST_CMD, userList));
    }

    public void sendStopServerMessage() throws IOException {
        out.writeUTF(END_CMD_PREFIX);
    }
}
