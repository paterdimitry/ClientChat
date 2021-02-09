package client.views;

import client.controllers.AuthController;
import client.controllers.ChatController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import client.models.Network;

import java.io.IOException;

public class ChatClient extends Application {

    private Stage primaryStage;
    private Network network;
    private Stage authStage;
    private ChatController chatController;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;
        network = new Network();
        network.connect();
        openAuthDialog();
        createChatWindow();


    }

    private void openAuthDialog() throws IOException {
        FXMLLoader authLoader = new FXMLLoader();
        authLoader.setLocation(ChatClient.class.getResource("/client/fxml/AuthWindow.fxml"));
        Parent root = authLoader.load();
        authStage = new Stage();
        authStage.setTitle("Аутентификация");
        authStage.setScene(new Scene(root));
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.initOwner(primaryStage);
        authStage.setMinWidth(400);
        authStage.setMinHeight(300);
        authStage.show();

        AuthController authController = authLoader.getController();
        authController.setNetwork(network);
        authController.setChatClient(this);
    }

    private void createChatWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ChatClient.class.getResource("/client/fxml/MainWindow.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Чат");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(660);
        primaryStage.setMinHeight(530);
        chatController = loader.getController();
        chatController.setNetwork(network);

    }


    public static void main(String[] args) {
        launch(args);
    }

    public void openChat() {
        authStage.close();
        primaryStage.show();
        primaryStage.setTitle(String.format("Чат (%s)",network.getUsername()));
        network.waitMessage(chatController);
    }
}
