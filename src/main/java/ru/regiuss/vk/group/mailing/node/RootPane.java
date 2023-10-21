package ru.regiuss.vk.group.mailing.node;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import ru.regiuss.vk.group.mailing.VkGroupApp;

@Getter
public class RootPane extends StackPane {

    @FXML
    private StackPane screen;

    @FXML
    private VBox sideBarBox;

    private final VkGroupApp app;

    public RootPane(VkGroupApp app) {
        this.app = app;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/root.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ToggleGroup menuToggleGroup = new ToggleGroup();
        sideBarBox.getChildren().addAll(
                new MenuButton(
                        new Image("/img/group.png"),
                        new Image("/img/group-selected.png"),
                        menuToggleGroup,
                        app::openGroupScreen
                ),
                new MenuButton(
                        new Image("/img/bookmark.png"),
                        new Image("/img/bookmark-selected.png"),
                        menuToggleGroup,
                        () -> {}
                ),
                new MenuButton(
                        new Image("/img/messages.png"),
                        new Image("/img/messages-selected.png"),
                        menuToggleGroup,
                        () -> {}
                ),
                new MenuButton(
                        new Image("/img/accounts.png"),
                        new Image("/img/accounts-selected.png"),
                        menuToggleGroup,
                        () -> {}
                )
        );
    }
}
