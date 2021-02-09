package client.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import client.models.Network;

public class ChatController {

    @FXML
    private ListView<String> listViewPerson;

    @FXML
    private ListView<String> listViewMsg;

    @FXML
    private TextField inputField;

    public Network network;

    public void setNetwork(Network network) {
        this.network = network;
    }

    private final ObservableList<String> msgList = FXCollections.observableArrayList("Добро пожаловать в чат!");

    private ObservableList<String> prsnList = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        listViewMsg.setItems(msgList);

        listViewPerson.setItems(prsnList);
        //реализация переноса строки внутри ячейки. решение взято с просторов Интернета
        listViewMsg.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setMinWidth(param.getWidth());
                    setMaxWidth(param.getWidth());
                    setPrefWidth(param.getWidth());
                    setWrapText(true);
                    setText(item);
                }
            }
        });
    }

    @FXML
    void sendMsg() {
        if (!inputField.getText().isBlank()) {
            if (inputField.getText().startsWith(Network.PRIVATE_MSG_CMD_PREFIX)) {
                network.sendPrivateMessage(inputField.getText().trim());
            } else {
                network.sendMessage(inputField.getText().trim());
            }
            inputField.clear();
        } else {
            return;
        }
        inputField.requestFocus();
    }

    //Для отправки личных сообщений по нажатию на ник в ListViewPerson просто в строку ввода добавляем префикс и username адресата
    @FXML
    void sendPrivateMessage(){
        String recipient = listViewPerson.getSelectionModel().getSelectedItem();
        if (!recipient.equals(network.getUsername())) {
            inputField.setText(String.format("%s %s ", Network.PRIVATE_MSG_CMD_PREFIX, recipient));
            inputField.requestFocus();
        }
    }

    @FXML
    void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О программе");
        alert.setHeaderText("Пользовательский чат v.0.3b");
        alert.setContentText("Пользовательский текстовый чат");
        alert.show();
    }

    @FXML
    void doExit() {
        System.exit(0);
    }


    public void sendMessageToList(String message) {
        listViewMsg.getItems().add(message);
    }

    public void addClientToList(String username) {
        listViewPerson.getItems().add(username);
        listViewPerson.getItems().sort(null);
    }

    public void resetUserList() {
        listViewPerson.getItems().clear();
    }
}
