package client.controllers;

import client.models.Network;
import client.views.ChatClient;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AuthController {

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;
    private Network network;
    private ChatClient mainChatClient;

    @FXML
    void doAuth() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.length() == 0 || password.length() == 0) {
            return;
        }
        String authErrorMessage = network.sendAuthMessage(login, password);
        if (authErrorMessage == null) {
            mainChatClient.openChat();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка авторизации");
            alert.setContentText(authErrorMessage);
            alert.show();
        }
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setChatClient(ChatClient chatClient) {
        this.mainChatClient = chatClient;
    }
}