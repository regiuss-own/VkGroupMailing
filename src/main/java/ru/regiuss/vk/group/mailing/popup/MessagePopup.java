package ru.regiuss.vk.group.mailing.popup;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import lombok.Setter;
import ru.regiuss.vk.group.mailing.model.Message;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class MessagePopup extends StackPane implements Initializable {

    private static FXMLLoader loader;

    @FXML
    private TextArea textArea;
    @Setter
    private Runnable onClose;
    @Setter
    private Consumer<Message> onMessage;
    private Message message;

    public MessagePopup() {
        if (loader == null)
            loader = new FXMLLoader(getClass().getResource("/view/messagePopup.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setMessage(Message message) {
        this.message = message;
        textArea.setText(message.getText());
    }

    @FXML
    void onAddFile(ActionEvent event) {

    }

    @FXML
    void onClose(ActionEvent event) {
        onClose.run();
    }

    @FXML
    void onClickBackground(MouseEvent event) {
        onClose.run();
    }

    @FXML
    void onSave(ActionEvent event) {
        if (textArea.getText() == null || textArea.getText().trim().isEmpty()) {
            onMessage.accept(null);
        } else {
            if (message == null)
                message = new Message();
            message.setText(textArea.getText());
            onMessage.accept(message);
        }
        onClose.run();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
