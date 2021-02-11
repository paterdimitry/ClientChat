package client.controllers;

import client.models.Network;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ChangeUsernameController {

    public static Stage changeUsernameStage;
    public Network network;

    @FXML
    private TextField newUsername;
    @FXML
    private PasswordField passwordField;

    @FXML
    void doChangeUsername() {
        String newUsernameText = newUsername.getText().trim();
        String passwordFieldText = passwordField.getText().trim();
        if (newUsernameText.length() == 0 || passwordFieldText.length() == 0)
            return;
        network.sendChangeUsername(newUsernameText, passwordFieldText);
    }


    public static void errorUpdateUsername(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setContentText(message);
        alert.show();
    }

    public static void okUpdateUsername(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Имя пользователя изменено");
        alert.setContentText("Ваше имя изменено на " + message);
        alert.show();
        changeUsernameStage.close();
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

}

