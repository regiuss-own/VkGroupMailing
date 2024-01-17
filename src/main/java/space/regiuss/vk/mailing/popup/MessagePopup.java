package space.regiuss.vk.mailing.popup;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import space.regiuss.vk.mailing.VkMailingApp;
import space.regiuss.vk.mailing.model.Attachment;
import space.regiuss.vk.mailing.model.Message;
import space.regiuss.rgfx.RGFXAPP;
import space.regiuss.rgfx.popup.BackgroundPopup;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@RequiredArgsConstructor
public class MessagePopup extends BackgroundPopup {

    private static FXMLLoader loader;

    @FXML
    private CheckBox dontParseLinkCheckBox;
    @FXML
    private TextArea attachmentArea;
    @FXML
    private TextArea textArea;
    @FXML
    private FlowPane attachmentsFlow;
    @Setter
    private Consumer<Message> onMessage;
    private Message message;
    private LinkedList<Attachment> attachments;
    private final VkMailingApp app;

    {
        RGFXAPP.load(this, getClass().getResource("/view/popup/messagePopup.fxml"));
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
        if (Objects.nonNull(message.getDontParseLink())) {
            dontParseLinkCheckBox.setSelected(message.getDontParseLink());
        }
        if (Objects.nonNull(message.getAttachmentLinkText())) {
            attachmentArea.setText(message.getAttachmentLinkText());
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
            attachment.setFilePatch(f.getAbsolutePath());
            attachment.setDocument(false);
            HBox hBox = createFileView(attachment);
            attachmentsFlow.getChildren().add(hBox);
        }

    }

    private HBox createFileView(Attachment attachment) {
        Label label = new Label(new File(attachment.getFilePatch()).getName());
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
            message.setDontParseLink(dontParseLinkCheckBox.isSelected());
            message.setAttachmentLinkText(attachmentArea.getText());
            onMessage.accept(message);
        }
        onClose.run();
    }
}
