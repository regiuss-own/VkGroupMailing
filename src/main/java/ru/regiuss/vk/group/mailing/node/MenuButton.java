package ru.regiuss.vk.group.mailing.node;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Paint;

public class MenuButton extends RadioButton {

    private final Image image;
    private final Image imageSelected;
    private final ImageView view;

    public MenuButton(Image image, Image imageSelected, ToggleGroup toggleGroup, Runnable onClick) {
        this.image = image;
        this.imageSelected = imageSelected;
        setToggleGroup(toggleGroup);
        getStyleClass().remove("radio-button");
        setPadding(new Insets(5));
        getStyleClass().add("menu-button");
        view = new ImageView();
        view.setImage(image);
        setPrefSize(50, 50);
        view.setFitWidth(45);
        setAlignment(Pos.CENTER);
        setGraphicTextGap(0);
        setBackground(new Background(new BackgroundFill(Paint.valueOf("red"), null, null)));
        view.setFitHeight(45);
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
}
