package ru.regiuss.vk.group.mailing.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import ru.regiuss.vk.group.mailing.VkGroupApp;
import ru.regiuss.vk.group.mailing.messenger.VkMessenger;
import ru.regiuss.vk.group.mailing.model.User;

public class MainController {

    private final VkGroupApp app;

    @FXML
    private TextField dialogDelayField;

    @FXML
    private TextField messageDelayField;

    @FXML
    private ListView<?> messagesList;

    @FXML
    private TextField minSubCountField;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private TextField searchField;

    @FXML
    private Label sendCountStatus;

    @FXML
    private HBox settingsPane;

    @FXML
    private Label timeStatus;

    @FXML
    private TextField tokenField;

    @FXML
    private Label tokenStatus;

    public MainController(VkGroupApp app) {
        this.app = app;
    }

    @FXML
    void onAddMessage(ActionEvent event) {

    }

    @FXML
    void onAuth(ActionEvent event) {

    }

    @FXML
    void onCheckToken(ActionEvent event) {
        final String token = tokenField.getText();
        app.getExecutorService().execute(() -> {
            final User user = VkMessenger.getUser(token);
            Platform.runLater(() -> {
                if (user == null)
                    tokenStatus.setText("Ошибка");
                else
                    tokenStatus.setText("Рабочий");
            });
        });
    }

    @FXML
    void onDeleteMessage(ActionEvent event) {

    }

    @FXML
    void onEditMessage(ActionEvent event) {

    }

    @FXML
    void onStart(ActionEvent event) {

    }
}
