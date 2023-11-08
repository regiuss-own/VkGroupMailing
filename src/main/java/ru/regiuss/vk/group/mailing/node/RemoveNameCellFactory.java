package ru.regiuss.vk.group.mailing.node;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Paint;
import lombok.Setter;
import ru.regiuss.vk.group.mailing.NameProvider;
import space.regiuss.rgfx.node.Icon;

import java.util.function.Consumer;

public class RemoveNameCellFactory<T extends NameProvider> extends NameCellFactory<T> {

    protected final Button removeButton;
    @Setter
    private Consumer<T> removeConsumer;

    public RemoveNameCellFactory() {
        Icon removeIcon = new Icon(Icon.IconValue.XMARK, Icon.IconType.SOLID, 20, Paint.valueOf("#000"));
        removeButton = new Button();
        removeButton.setText(null);
        removeButton.setGraphic(removeIcon);
        removeButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        removeButton.setStyle("-fx-background-color: transparent;");
        removeButton.setOnAction(event -> {
            if (removeConsumer == null || getItem() == null)
                return;
            removeConsumer.accept(getItem());
        });
        HBox buttonBox = new HBox(removeButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(buttonBox, Priority.ALWAYS);
        graphicNode.getChildren().add(buttonBox);
    }
}
