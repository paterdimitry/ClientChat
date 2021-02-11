package client.controllers;

import client.models.Network;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ChangePasswordController {

    public static Stage changePasswordStage;
    public Network network;

    @FXML
    private PasswordField pastPasswordField;

    @FXML
    private PasswordField passwordField1;

    @FXML
    private PasswordField passwordField2;

    @FXML
    void updatePassword() {
        String password = pastPasswordField.getText().trim();
        String newPassword = passwordField1.getText().trim();
        if (newPassword.equals(passwordField2.getText().trim())) {
            network.sendUpdatePassword(password, newPassword);
        } else {
            showNotEquallyPasswords();
        }
    }

    private void showNotEquallyPasswords() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка авторизации");
        alert.setContentText("Пароли не совпадают");
        alert.show();
    }

    public static void okUpdatePassword() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Успех");
        alert.setContentText("Ваш пароль успешно заменен");
        alert.show();
        changePasswordStage.close();
    }

    public static void errorUpdatePassword(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setContentText(message);
        alert.show();

    }

    public void setNetwork(Network network) {
        this.network = network;
    }
}
