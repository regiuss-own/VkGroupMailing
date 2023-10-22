package ru.regiuss.vk.group.mailing.node;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

    private final ToggleGroup menuToggleGroup = new ToggleGroup();

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
        sideBarBox.getChildren().addAll(
                new MenuButton(
                        new Image("/img/team.png", 32, 32, false, true),
                        new Image("/img/team-selected.png", 32, 32, false, true),
                        menuToggleGroup,
                        app::openGroupScreen
                ),
                new MenuButton(
                        new Image("/img/bookmark.png", 32, 32, false, true),
                        new Image("/img/bookmark-selected.png", 32, 32, false, true),
                        menuToggleGroup,
                        () -> {}
                ),
                new MenuButton(
                        new Image("/img/messages.png", 32, 32, false, true),
                        new Image("/img/messages-selected.png", 32, 32, false, true),
                        menuToggleGroup,
                        () -> {}
                ),
                new MenuButton(
                        new Image("/img/accounts.png", 32, 32, false, true),
                        new Image("/img/accounts-selected.png", 32, 32, false, true),
                        menuToggleGroup,
                        () -> {}
                )
        );
    }
}
