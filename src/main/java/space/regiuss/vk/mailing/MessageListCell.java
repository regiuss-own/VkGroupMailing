package space.regiuss.vk.mailing;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import space.regiuss.vk.mailing.model.Attachment;
import space.regiuss.vk.mailing.model.Message;

import java.io.File;
import java.util.Objects;

public class MessageListCell extends ListCell<Message> {
    @Override
    protected void updateItem(Message item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            VBox vBox = new VBox();
            vBox.setSpacing(5);
            Text text = new Text(item.getText());
            text.setFont(Font.font(16));
            text.setWrappingWidth(300);
            FlowPane flowPane = new FlowPane();
            flowPane.setMaxWidth(350);
            flowPane.setHgap(5);
            flowPane.setVgap(2);
            if (item.getAttachments() != null) {
                for (Attachment attachment : item.getAttachments()) {
                    Label label = new Label(new File(attachment.getFilePatch()).getName());
                    if (attachment.isDocument())
                        label.setStyle("-fx-border-color: green; -fx-border-radius: 5px; -fx-padding: 5px");
                    else
                        label.setStyle("-fx-border-color: #346df1; -fx-border-radius: 5px; -fx-padding: 5px");
                    flowPane.getChildren().add(label);
                }
                if (Objects.nonNull(item.getAttachmentLinkText()) && !item.getAttachmentLinkText().trim().isEmpty()) {
                    for (String attachment : item.getAttachmentLinkText().split("\n")) {
                        Label label = new Label(attachment);
                        label.setStyle("-fx-border-color: #346df1; -fx-border-radius: 5px; -fx-padding: 5px");
                        flowPane.getChildren().add(label);
                    }
                }
            }
            vBox.getChildren().addAll(text, flowPane);
            setGraphic(vBox);
        }
    }
}
