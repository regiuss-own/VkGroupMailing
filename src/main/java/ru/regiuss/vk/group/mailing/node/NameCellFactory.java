package ru.regiuss.vk.group.mailing.node;

import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import ru.regiuss.vk.group.mailing.NameProvider;

public class NameCellFactory<T extends NameProvider> extends ListCell<T> {

    protected final HBox graphicNode;
    protected final Text name;

    public NameCellFactory() {
        setText(null);
        name = new Text();
        name.setFont(Font.font(16));;
        graphicNode = new HBox(name);
        graphicNode.setSpacing(10);
        graphicNode.setPadding(new Insets(15, 10, 15, 10));
        graphicNode.getStyleClass().addAll("rounded-box", "selectable");
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            return;
        }
        name.setText(item.getName());
        setGraphic(graphicNode);
    }
}
