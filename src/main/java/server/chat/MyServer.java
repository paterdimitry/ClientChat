package server.chat;

import server.chat.service.classes.*;
import server.chat.handler.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.*;

public class MyServer {

    private final ServerSocket serverSocket;
    private final DBAuthService authService;
    private final DBRegService regService;
    private final DBChangeUsernameService changeUsernameService;
    private final DBChangePasswordService changePasswordService;

    private final List<ClientHandler> clients = new ArrayList<>();

    //создаем два логгера: для чата и для серверных сообщений
    protected final Logger serverLogger = Logger.getLogger("server");
    protected final Logger chatLogger = Logger.getLogger("chat");

    public Logger getServerLogger() {
        return serverLogger;
    }

    public MyServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.authService = new DBAuthService();
        this.regService = new DBRegService();
        this.changeUsernameService = new DBChangeUsernameService();
        this.changePasswordService = new DBChangePasswordService();
    }

    public void start() {
        startServerLogger();
        serverLogger.info("Сервер запущен");
        startChatLogger();
        try {
            while (true) {
                waitAndConnectClient();
            }
        } catch (IOException ioException) {
            serverLogger.severe("Клиенту не удалось присоединиться к серверу");
        }
    }

    private void startServerLogger() {
        //описываем параметры логирования
        serverLogger.setLevel(Level.ALL);
        try {
            Handler handler = new FileHandler("src/main/resources/serverLogs/serverLog.log");
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            serverLogger.addHandler(handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startChatLogger() {
        chatLogger.setLevel(Level.ALL);
        try {
            Handler handler = new FileHandler("src/main/resources/serverLogs/chatLog.log");
            //описываем формат логирования переписки для более удобного чтения
            handler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return record.getLevel() + " " + (new Date(record.getMillis())).toString() + " " + record.getMessage() + "\n";
                }
            });
            handler.setLevel(Level.ALL);
            chatLogger.addHandler(handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //убираем вывод логов переписки в консоль
        chatLogger.setUseParentHandlers(false);

    }

    private void waitAndConnectClient() throws IOException {
        Socket socket = serverSocket.accept();
        serverLogger.info("Установлено соединение с клиентом, ожидаем авторизацию");
        ClientHandler clientHandler = new ClientHandler(this, socket);
        clientHandler.handle();
    }

    public synchronized void subscribe(ClientHandler clientHandler) throws IOException {
        clients.add(clientHandler);
        broadcastUserList();
        broadcastServerMessage(clientHandler, "SUB");
        chatLogger.info(clientHandler.getUsername() + " подключился к чату");
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        try {
            broadcastUserList();
        } catch (IOException e) {
            serverLogger.warning("Ошибка передачи списка клиентов");
        }
        broadcastServerMessage(clientHandler, "UNSUB");
        chatLogger.info(clientHandler.getUsername() + " подключился к чату");
    }

    public synchronized boolean isUsernameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastServerMessage(ClientHandler user, String flag) {
        for (ClientHandler client : clients) {
            if (user != client) {
                try {
                    switch (flag) {
                        case "SUB" -> client.sendServerMessage(user.getUsername() + " подключился к чату");
                        case "UNSUB" -> client.sendServerMessage(user.getUsername() + " покинул чат");
                    }
                } catch (IOException e) {
                    serverLogger.warning("Ошибка рассылки серверных сообщений о подключении или отключении клиента");
                }
            }
        }
    }

    public synchronized void broadcastMessage(ClientHandler sender, String message) throws IOException {
        chatLogger.fine(sender.getUsername() + ": " + message);
        for (ClientHandler client : clients) {
            if (client == sender)
                client.sendMessage(message);
            else
                client.sendMessage(sender.getUsername(), message);
        }
    }

    public synchronized void broadcastUpdateUsernameMessage(ClientHandler user, String lastUsername, String username) {
        for (ClientHandler client : clients) {
            if (user != client) {
                try {
                    client.sendServerMessage(lastUsername + " сменил имя пользователя на " + username);
                    serverLogger.info(lastUsername + " сменил имя пользователя на " + username);
                } catch (IOException e) {
                    serverLogger.warning("Ошибка рассылки серверных сообщений о смене имени пользователя");
                }
            }
        }
    }

    //ретранслятор для личных сообщений
    public synchronized void broadcastMessage(ClientHandler sender, String recipient, String message) throws IOException {
        boolean flag = false;
        chatLogger.fine(sender.getUsername() + " -> " + recipient + ": " + message);
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(recipient)) {
                client.sendPrivateMessage(sender.getUsername(), recipient, message); //находим адресата и отправляем сообщение
                flag = true;
            }
        }
        if (flag)
            sender.sendPrivateMessage(recipient, message);
        else
            sender.sendPrivateMessage();
    }

    public synchronized void broadcastUserList() throws IOException {
        StringBuilder userList = new StringBuilder(" ");
        for (ClientHandler client : clients) {
            userList.append(client.getUsername()).append(" ");
        }
        for (ClientHandler client : clients) {
            client.sendUserList(userList.toString());
        }
    }

    public synchronized void stop() throws IOException {
        for (ClientHandler client : clients) {
            client.sendStopServerMessage();
        }
        serverLogger.info("Сервер отключен по команде клиента");
        System.exit(0);
    }

    public DBAuthService getAuthService() {
        return authService;
    }

    public DBRegService getRegService() {
        return regService;
    }

    public DBChangeUsernameService getChangeUsernameService() {
        return changeUsernameService;
    }

    public DBChangePasswordService getChangePasswordService() {
        return changePasswordService;
    }
}
