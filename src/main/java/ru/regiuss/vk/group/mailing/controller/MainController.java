package ru.regiuss.vk.group.mailing.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import lombok.extern.log4j.Log4j2;
import ru.regiuss.vk.group.mailing.VkGroupApp;
import ru.regiuss.vk.group.mailing.messenger.Messenger;
import ru.regiuss.vk.group.mailing.messenger.VkMessenger;
import ru.regiuss.vk.group.mailing.model.Message;
import ru.regiuss.vk.group.mailing.model.User;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;

@Log4j2
public class MainController implements Initializable {

    private final VkGroupApp app;

    @FXML
    private TextField dialogDelayField;

    @FXML
    private TextField messageDelayField;

    @FXML
    private ListView<Message> messagesList;

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
        messagesList.getItems().add(new Message("test", Collections.singletonList(new File("test"))));
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
    void onStart(ActionEvent event) throws Exception {
        Messenger messenger = new VkMessenger(tokenField.getText());
        log.info(messenger.search(1, "game"));
        messenger.send(214686349, new Message("asdsa", Arrays.asList(
                new File("C:\\Users\\root\\Downloads\\fav_logo_2x.png"),
                new File("C:\\Users\\root\\Downloads\\2023-03-30-23-45-15_1.mp4")
        )));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        messagesList.setCellFactory(new Callback<ListView<Message>, ListCell<Message>>() {
            @Override
            public ListCell<Message> call(ListView<Message> messageListView) {
                return null;
            }
        });
    }
}
