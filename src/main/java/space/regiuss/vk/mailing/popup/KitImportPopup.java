package space.regiuss.vk.mailing.popup;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.regiuss.vk.mailing.VkMailingApp;
import space.regiuss.vk.mailing.screen.BookmarkRunnableScreen;
import space.regiuss.vk.mailing.screen.GroupRunnableScreen;
import space.regiuss.vk.mailing.screen.ProfileRunnableScreen;
import space.regiuss.rgfx.RGFXAPP;
import space.regiuss.rgfx.node.Icon;
import space.regiuss.rgfx.popup.BackgroundPopup;

import javax.annotation.PostConstruct;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class KitImportPopup extends BackgroundPopup {

    private final VkMailingApp app;

    @FXML
    private TilePane screensBox;

    @FXML
    private TilePane savedBox;

    {
        RGFXAPP.load(this, getClass().getResource("/view/popup/kitImport.fxml"));
    }

    @PostConstruct
    public void init() {
        addButton(screensBox, "По группам", Icon.IconValue.COMPASS, Icon.IconType.REGULAR, event -> {
            app.getScreen(GroupRunnableScreen.class).getCurrentKitView().onMailingClick(null);
            onClose.run();
        });
        addButton(screensBox, "По избранное", Icon.IconValue.BOOKMARK, Icon.IconType.REGULAR, event -> {
            app.getScreen(BookmarkRunnableScreen.class).getCurrentKitView().onMailingClick(null);
            onClose.run();
        });
        addButton(screensBox, "По профилям", Icon.IconValue.USER, Icon.IconType.REGULAR, event -> {
            app.getScreen(ProfileRunnableScreen.class).getCurrentKitView().onMailingClick(null);
            onClose.run();
        });
    }

    private void addButton(TilePane container, String text, Icon.IconValue iconValue, Icon.IconType iconType, EventHandler<ActionEvent> onAction) {
        Button button = new Button(text);
        button.setOnAction(onAction);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setGraphicTextGap(15);
        button.setPadding(new Insets(20));
        button.setPrefWidth(200);
        button.setFont(Font.font(18));
        button.getStyleClass().add("white");
        if (container.equals(screensBox))
            button.setGraphic(new Icon(iconValue, iconType, 18, Paint.valueOf("#000")));
        container.getChildren().add(button);
    }
}
