package ru.regiuss.vk.group.mailing.node;

import javafx.application.HostServices;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import ru.regiuss.vk.group.mailing.model.Page;
import ru.regiuss.vk.group.mailing.model.ProgressItemWrapper;
import space.regiuss.rgfx.node.Icon;

public class ProgressPageListItem extends PageListItem<ProgressItemWrapper<Page>> {

    protected final HBox statusBox;
    private final Icon statusIcon;
    private final Text statusText;

    public ProgressPageListItem(HostServices hostServices) {
        super(hostServices);
        statusIcon = new Icon();
        statusIcon.setType(Icon.IconType.SOLID);
        statusIcon.setSize(20);
        statusText = new Text();
        statusText.setFont(Font.font(20));
        statusBox = new HBox(statusIcon, statusText);
        statusBox.setSpacing(10);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(statusBox, Priority.ALWAYS);
        hBox.getChildren().add(statusBox);
    }

    @Override
    protected void updateItem(ProgressItemWrapper<Page> wrapper, boolean empty) {
        super.updateItem(wrapper, empty);
        if (wrapper == null || empty)
            return;
        if (wrapper.getProgress() < 0 || wrapper.getTotal() < 0) {
            statusBox.setVisible(false);
            statusBox.setManaged(false);
            return;
        }
        statusBox.setVisible(true);
        statusBox.setManaged(true);
        statusText.setText(String.format("%s / %s", wrapper.getProgress(), wrapper.getTotal()));
        Paint fillColor;
        if (wrapper.getProgress() == 0) {
            fillColor = Paint.valueOf("red");
            statusIcon.setValue(Icon.IconValue.CIRCLE_EXCLAMATION);

        } else if (wrapper.getProgress() == wrapper.getTotal()) {
            fillColor = Paint.valueOf("green");
            statusIcon.setValue(Icon.IconValue.CIRCLE_CHECK);
        } else {
            fillColor = Paint.valueOf("orange");
            statusIcon.setValue(Icon.IconValue.TRIANGLE_EXCLAMATION);
        }
        statusText.setFill(fillColor);
        statusIcon.setFill(fillColor);
    }
}
