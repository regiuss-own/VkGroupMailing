package ru.regiuss.vk.group.mailing.popup;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import lombok.Setter;
import ru.regiuss.vk.group.mailing.VkGroupApp;
import ru.regiuss.vk.group.mailing.model.Attachment;
import ru.regiuss.vk.group.mailing.model.Message;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class MessagePopup extends StackPane {

    private static FXMLLoader loader;

    @FXML
    private TextArea textArea;
    @FXML
    private FlowPane attachmentsFlow;
    @Setter
    private Runnable onClose;
    @Setter
    private Consumer<Message> onMessage;
    private Message message;
    private LinkedList<Attachment> attachments;
    private final VkGroupApp app;

    public MessagePopup(VkGroupApp app) {
        this.app = app;
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
        if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
            attachments = new LinkedList<>(message.getAttachments());
            attachmentsFlow.getChildren().clear();
            for (Attachment attachment : attachments) {
                HBox hBox = createFileView(attachment);
                attachmentsFlow.getChildren().add(hBox);
            }
        }
    }

    @FXML
    void onAddFile(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        List<File> files = chooser.showOpenMultipleDialog(app.getStage());
        if (files == null || files.isEmpty())
            return;
        if (attachments == null)
            attachments = new LinkedList<>();
        for (File f : files) {
            Attachment attachment = new Attachment();
            attachments.add(attachment);
            attachment.setFile(f);
            attachment.setDocument(false);
            HBox hBox = createFileView(attachment);
            attachmentsFlow.getChildren().add(hBox);
        }

    }

    private HBox createFileView(Attachment attachment) {
        Label label = new Label(attachment.getFile().getName());
        Button removeButton = new Button("X");
        HBox hBox = new HBox(label, removeButton);
        hBox.setSpacing(5);
        hBox.setAlignment(Pos.CENTER_LEFT);
        toggleDocument(attachment, hBox);
        hBox.setOnMouseClicked(mouseEvent -> {
            attachment.setDocument(!attachment.isDocument());
            toggleDocument(attachment, hBox);
        });
        removeButton.setOnAction(event1 -> {
            attachments.remove(attachment);
            attachmentsFlow.getChildren().remove(hBox);
        });
        return hBox;
    }

    private void toggleDocument(Attachment attachment, HBox hBox) {
        if (attachment.isDocument())
            hBox.setStyle("-fx-border-color: green; -fx-border-radius: 5px; -fx-padding: 5px");
        else
            hBox.setStyle("-fx-border-color: #346df1; -fx-border-radius: 5px; -fx-padding: 5px");
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
        if ((attachments == null || attachments.isEmpty()) && (textArea.getText() == null || textArea.getText().trim().isEmpty())) {
            onMessage.accept(null);
        } else {
            if (message == null)
                message = new Message();
            message.setText(textArea.getText());
            message.setAttachments(attachments);
            onMessage.accept(message);
        }
        onClose.run();
    }
}
