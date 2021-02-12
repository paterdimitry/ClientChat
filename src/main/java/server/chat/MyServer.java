package server.chat;

import server.chat.service.classes.DBAuthService;
import server.chat.service.classes.DBChangePasswordService;
import server.chat.service.classes.DBChangeUsernameService;
import server.chat.service.classes.DBRegService;
import server.chat.handler.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {

    private final ServerSocket serverSocket;
    private final DBAuthService authService;
    private final DBRegService regService;
    private final DBChangeUsernameService changeUsernameService;
    private final DBChangePasswordService changePasswordService;

    private final List<ClientHandler> clients = new ArrayList<>();

    public MyServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.authService = new DBAuthService();
        this.regService = new DBRegService();
        this.changeUsernameService = new DBChangeUsernameService();
        this.changePasswordService = new DBChangePasswordService();
    }

    public void start() {
        System.out.println("Server started!");

        try {
            while (true) {
                waitAndConnectClient();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void waitAndConnectClient() throws IOException {
        System.out.println("Waiting connection");
        Socket socket = serverSocket.accept();
        System.out.println("Client connected");
        ClientHandler clientHandler = new ClientHandler(this, socket);
        clientHandler.handle();
    }

    public synchronized void subscribe(ClientHandler clientHandler) throws IOException {
        clients.add(clientHandler);
        broadcastUserList();
        broadcastServerMessage(clientHandler, "SUB");
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        try {
            broadcastUserList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        broadcastServerMessage(clientHandler, "UNSUB");
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
                    System.out.println("Ошибка рассылки серверных сообщений");
                }
            }
        }
    }

    //ретранслятор для общих сообщений

    public synchronized void broadcastMessage(ClientHandler sender, String message) throws IOException {
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
                } catch (IOException e) {
                    System.out.println("Ошибка рассылки серверных сообщений");
                }
            }
        }
    }

//ретранслятор для личных сообщений
    public synchronized void broadcastMessage(ClientHandler sender, String recipient, String message) throws IOException {
        boolean flag = false;
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
        System.exit(0);
    }

    public DBAuthService getAuthService() {
        return authService;
    }

    public DBRegService getRegService() { return regService; }

    public DBChangeUsernameService getChangeUsernameService() {
        return changeUsernameService;
    }

    public DBChangePasswordService getChangePasswordService() {
        return changePasswordService;
    }
}
