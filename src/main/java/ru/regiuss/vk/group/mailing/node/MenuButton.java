package ru.regiuss.vk.group.mailing.node;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Paint;

public class MenuButton extends ToggleButton {

    private final Image image;
    private final Image imageSelected;
    private final ImageView view;

    public MenuButton(Image image, Image imageSelected, ToggleGroup toggleGroup, Runnable onClick) {
        this.image = image;
        this.imageSelected = imageSelected;
        setToggleGroup(toggleGroup);
        setMinWidth(60);
        setMaxWidth(60);
        setPadding(new Insets(5));
        getStyleClass().add("menu-button");
        view = new ImageView();
        view.setImage(image);
        setAlignment(Pos.CENTER);
        setGraphicTextGap(0);
        setGraphic(view);
        selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                view.setImage(imageSelected);
                onClick.run();
            } else {
                view.setImage(image);
            }
        });
    }

    @Override
    public void fire() {
        if (this.getToggleGroup() == null || !this.isSelected()) {
            super.fire();
        }
    }
}
