package space.regiuss.vk.mailing.node;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;
import lombok.Getter;
import space.regiuss.vk.mailing.model.Account;
import space.regiuss.rgfx.RGFXAPP;
import space.regiuss.rgfx.enums.AlertVariant;
import space.regiuss.rgfx.node.Icon;
import space.regiuss.rgfx.node.SimpleAlert;

@Getter
public class AccountManageItem extends StackPane {
    private final AccountItem item;
    protected final Button removeButton;

    public AccountManageItem(Account account, RGFXAPP<?> app) {
        item = new AccountItem(account);
        removeButton = createIconButton(Icon.IconValue.TRASH_CAN, Icon.IconType.REGULAR, "Удалить");
        Button openButton = createIconButton(Icon.IconValue.ARROW_UP_RIGHT_FROM_SQUARE, Icon.IconType.SOLID, "Открыть страницу");
        openButton.setOnAction(event -> app.getHostServices().showDocument("https://vk.com/id" + account.getId()));
        Button copyButton = createIconButton(Icon.IconValue.COPY, Icon.IconType.REGULAR, "Скопировать токен");
        copyButton.setOnAction(event -> {
            final ClipboardContent content = new ClipboardContent();
            content.putString(account.getToken());
            Clipboard.getSystemClipboard().setContent(content);

            Node oldGraphic = copyButton.getGraphic();
            copyButton.setGraphic(new Icon(Icon.IconValue.CIRCLE_CHECK, Icon.IconType.REGULAR, 20, Paint.valueOf("green")));
            copyButton.setDisable(true);
            PauseTransition transition = new PauseTransition();
            transition.setDelay(Duration.seconds(2));
            transition.setOnFinished(event1 -> {
                copyButton.setGraphic(oldGraphic);
                copyButton.setDisable(false);
            });
            transition.play();
            app.showAlert(new SimpleAlert("Токен скопирован в буфер обмена", AlertVariant.SUCCESS), Duration.seconds(5));
        });
        HBox buttons = new HBox(
                openButton,
                copyButton,
                removeButton
        );
        buttons.setSpacing(5);
        buttons.setPadding(new Insets(5));
        buttons.setAlignment(Pos.BOTTOM_RIGHT);
        getChildren().addAll(item, buttons);
    }

    public void setOnRemove(EventHandler<ActionEvent> onRemove) {
        removeButton.setOnAction(onRemove);
    }

    private Button createIconButton(Icon.IconValue icon, Icon.IconType type, String tooltipText) {
        Button button = new Button();
        button.getStyleClass().add("white");
        button.setGraphic(new Icon(icon, type, 20, Paint.valueOf("#555")));
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setFont(Font.font(16));
        tooltip.setStyle("-fx-background-color: #ffffff33");
        button.setTooltip(tooltip);
        return button;
    }
}
