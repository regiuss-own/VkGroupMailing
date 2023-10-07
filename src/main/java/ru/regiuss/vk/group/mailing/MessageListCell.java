package ru.regiuss.vk.group.mailing;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ru.regiuss.vk.group.mailing.model.Message;

import java.io.File;

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
            text.setWrappingWidth(300);
            FlowPane flowPane = new FlowPane();
            flowPane.setMaxWidth(350);
            flowPane.setHgap(5);
            flowPane.setVgap(2);
            for (File file : item.getFiles()) {
                Label label = new Label(file.getName());
                label.setStyle("-fx-background-color: #346df1; -fx-text-fill: #fff; -fx-background-radius: 5px; -fx-padding: 5px");
                flowPane.getChildren().add(label);
            }
            vBox.getChildren().addAll(text, flowPane);
            setGraphic(vBox);
        }
    }
}
