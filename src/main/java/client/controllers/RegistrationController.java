
package client.controllers;

import client.models.Network;
import client.views.ChatClient;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class RegistrationController {

    private Network network;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameField;

    @FXML
    void doRegistration() throws IOException {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        String username = usernameField.getText().trim();

        if (login.length() == 0 | password.length() == 0 | username.length() == 0)
            return;
        String regErrorMessage = network.sendRegMessage(login, password, username);
        if (regErrorMessage == null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Регистрация");
            alert.setContentText("Вы успешно зарегистрированы. Переходите к авторизации.");
            alert.showAndWait();
            ChatClient.setAuthScene("AuthWindow");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка регистрации");
            alert.setContentText(regErrorMessage);
            alert.show();
        }
    }

    @FXML
    void goBack() throws IOException {
        ChatClient.setAuthScene("AuthWindow");
    }

    public void setNetwork(Network network) {
        this.network = network;
    }
}


