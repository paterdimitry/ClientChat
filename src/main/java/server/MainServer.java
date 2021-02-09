package server;

import server.chat.MyServer;
import java.io.IOException;

public class MainServer {

    private static final int DEFAULT_SERVER_PORT = 8888;

    public static void main(String[] args) {

        int port = DEFAULT_SERVER_PORT;

        try {

            new MyServer(port).start();

        } catch (IOException e) {
            System.out.println("Ошибка запуска сервера");
            System.exit(1);
        }
    }

}