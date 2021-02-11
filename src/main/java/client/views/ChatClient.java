package client.views;

import client.controllers.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import client.models.Network;

import java.io.IOException;

public class ChatClient extends Application {

    private static Stage primaryStage;
    private static Network network;
    private static Stage authStage;
    private static ChatController chatController;
    private static Scene authScene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        network = new Network();
        network.connect();
        openAuthDialog();
        createChatWindow();
    }

    private void openAuthDialog() throws IOException {
        authStage = new Stage();
        authStage.setTitle("Аутентификация");
        authScene = new Scene(new Parent() { });
        setAuthScene("AuthWindow");
        authStage.setScene(authScene);
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.initOwner(primaryStage);
        authStage.setMinWidth(300);
        authStage.setMinHeight(250);
        authStage.show();
    }

    public static void setAuthScene(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ChatClient.class.getResource("/fxml/" + fxml + ".fxml"));
        authScene.setRoot(loader.load());
        switch (fxml) {
            case "AuthWindow" -> {
                AuthController authController = loader.getController();
                authController.setNetwork(network);
            }
            case "RegistrationWindow" -> {
                RegistrationController registrationController = loader.getController();
                registrationController.setNetwork(network);
            }
        }
    }

    private static void createChatWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ChatClient.class.getResource("/fxml/MainWindow.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Чат");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(660);
        primaryStage.setMinHeight(530);
        chatController = loader.getController();
        chatController.setNetwork(network);

    }

    public static void createChangeUsernameWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ChatClient.class.getResource("/fxml/ChangeUsernameWindow.fxml"));
        AnchorPane dialog = loader.load();
        Stage changeUsernameStage = new Stage();
        changeUsernameStage.setTitle("Изменение никнейма");
        changeUsernameStage.setScene(new Scene(dialog));
        ChangeUsernameController changeUsernameController = loader.getController();
        changeUsernameController.changeUsernameStage = changeUsernameStage;
        changeUsernameController.setNetwork(network);
        changeUsernameStage.show();
    }

    public static void createChangePasswordWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ChatClient.class.getResource("/fxml/ChangePasswordWindow.fxml"));
        AnchorPane dialog = loader.load();
        Stage changePasswordStage = new Stage();
        changePasswordStage.setTitle("Изменение пароля");
        changePasswordStage.setScene(new Scene(dialog));
        ChangePasswordController changePasswordController = loader.getController();
        changePasswordController.changePasswordStage = changePasswordStage;
        changePasswordController.setNetwork(network);
        changePasswordStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void openChat() {
        authStage.close();
        primaryStage.show();
        changeStageTitle();
        network.waitMessage(chatController);
    }

    public static void changeStageTitle() {
        primaryStage.setTitle(String.format("Чат (%s)", network.getUsername()));
    }
}
