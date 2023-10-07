package ru.regiuss.vk.group.mailing.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import lombok.extern.log4j.Log4j2;
import ru.regiuss.vk.group.mailing.MessageListCell;
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
    private StackPane rootPane;

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
        messagesList.getItems().add(new Message("Lorem ipsum — название классического текста-«рыбы». «Рыба» — слово из жаргона дизайнеров, обозначает условный, зачастую бессмысленный текст, вставляемый в макет страницы. Lorem ipsum представляет собой искажённый отрывок из философского трактата Цицерона «О пределах добра и зла», написанного в 45 году до нашей эры на латинском языке.", Arrays.asList(
                new File("C:\\Users\\root\\Downloads\\fav_logo_2x.png"),
                new File("C:\\Users\\root\\Downloads\\fav_logo_2x.png"),
                new File("C:\\Users\\root\\Downloads\\fav_logo_2x.png"),
                new File("C:\\Users\\root\\Downloads\\fav_logo_2x.png"),
                new File("C:\\Users\\root\\Downloads\\fav_logo_2x.png"),
                new File("C:\\Users\\root\\Downloads\\fav_logo_2x.png"),
                new File("C:\\Users\\root\\Downloads\\fav_logo_2x.png"),
                new File("C:\\Users\\root\\Downloads\\fav_logo_2x.png"),
                new File("C:\\Users\\root\\Downloads\\fav_logo_2x.png"),
                new File("C:\\Users\\root\\Downloads\\fav_logo_2x.png"),
                new File("C:\\Users\\root\\Downloads\\fav_logo_2x.png"),
                new File("C:\\Users\\root\\Downloads\\2023-03-30-23-45-15_1.mp4")
        )));
    }

    @FXML
    void onAuth(ActionEvent event) {
        AuthPopup popup = new AuthPopup();
        popup.setOnClose(() -> rootPane.getChildren().remove(popup));
        popup.setOnToken(token -> tokenField.setText(token));
        rootPane.getChildren().add(popup);
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
        messagesList.setCellFactory(messageListView -> new MessageListCell());
    }
}
