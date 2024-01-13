package space.regiuss.vk.mailing.popup;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.regiuss.rgfx.RGFXAPP;
import space.regiuss.rgfx.enums.AlertVariant;
import space.regiuss.rgfx.node.Icon;
import space.regiuss.rgfx.node.SimpleAlert;
import space.regiuss.rgfx.popup.BackgroundPopup;
import space.regiuss.vk.mailing.VkMailingApp;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.messenger.VkMessenger;
import space.regiuss.vk.mailing.model.Account;
import space.regiuss.vk.mailing.model.ImageItemWrapper;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.model.PageType;
import space.regiuss.vk.mailing.screen.BookmarkRunnableScreen;
import space.regiuss.vk.mailing.screen.GroupRunnableScreen;
import space.regiuss.vk.mailing.screen.MailingRunnableScreen;
import space.regiuss.vk.mailing.screen.ProfileRunnableScreen;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class KitImportPopup extends BackgroundPopup {

    private final VkMailingApp app;
    private MailingRunnableScreen screen;
    private final PseudoClass HOVER = PseudoClass.getPseudoClass("hover");

    @FXML
    private VBox importBox;

    @FXML
    private TilePane screensBox;

    @FXML
    private TilePane savedBox;

    {
        RGFXAPP.load(this, getClass().getResource("/view/popup/kitImport.fxml"));
        importBox.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (
                        event.getGestureSource() != importBox
                                && event.getDragboard().hasFiles()
                        && event.getDragboard().getFiles().size() == 1
                        && event.getDragboard().getFiles().get(0).getName().endsWith(".csv")
                ) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    importBox.pseudoClassStateChanged(HOVER, true);
                }
                event.consume();
            }
        });
        importBox.setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent dragEvent) {
                importBox.pseudoClassStateChanged(HOVER, false);
            }
        });
        importBox.pseudoClassStateChanged(HOVER, false);
        importBox.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles() && db.getFiles().size() == 1 && db.getFiles().get(0).getName().endsWith(".csv")) {
                    importFromFile(db.getFiles().get(0));
                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
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
        screen = app.getScreen(MailingRunnableScreen.class);
    }

    @FXML
    public void onImportClick(MouseEvent event) {
        if (screen.getSelectAccountButton().getCurrentAccount().get() == null) {
            app.showAlert(new SimpleAlert("Для импорта необходимо указать аккаунт", AlertVariant.SUCCESS), Duration.seconds(5));
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("csv", "*.csv"));
        File file = chooser.showOpenDialog(app.getStage());
        if (file != null)
            importFromFile(file);
    }

    private void importFromFile(File file) {
        onClose.run();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        app.showAlert(new SimpleAlert("Начинаю импорт", AlertVariant.SUCCESS), Duration.seconds(5));
        completableFuture.whenComplete((r, e) -> Platform.runLater(() -> {
            if (e == null) {
                app.showAlert(new SimpleAlert("Импорт завершен", AlertVariant.SUCCESS), Duration.seconds(5));
            } else {
                log.warn("import error", e);
                app.showAlert(new SimpleAlert("Ошибка импорта: " + e.getMessage(), AlertVariant.DANGER), Duration.seconds(5));
            }
        }));
        Account account = screen.getSelectAccountButton().getCurrentAccount().get();
        Messenger messenger = new VkMessenger(account.getToken());
        app.getExecutorService().execute(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                List<ImageItemWrapper<Page>> items = new LinkedList<>();
                String line = reader.readLine();
                List<String> pageIds = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty())
                        continue;
                    String[] data = line.trim().split(";");
                    if (data.length < 6 && data.length > 1)
                        throw new RuntimeException("Неверный формат");
                    if (data.length == 1) {
                        String pageId = data[0];
                        int index = pageId.lastIndexOf('/');
                        if (index != -1) {
                            pageId = pageId.substring(index + 1);
                        }
                        index = pageId.indexOf('?');
                        if (index != -1) {
                            pageId = pageId.substring(0, index);
                        }
                        pageIds.add(pageId);
                        if (pageIds.size() >= 10) {
                            getPagesByIds(messenger, pageIds, items);
                        }
                    } else {
                        Page page = new Page();
                        page.setId(Integer.parseInt(data[1]));
                        page.setType(PageType.valueOf(data[2]));
                        page.setName(data[3]);
                        page.setSubscribers(Integer.parseInt(data[4]));
                        page.setIcon(data[5]);
                        ImageItemWrapper<Page> item = new ImageItemWrapper<>(page);
                        items.add(item);
                    }
                }
                getPagesByIds(messenger, pageIds, items);
                if (items.isEmpty())
                    throw new RuntimeException("Файл пуст");
                Platform.runLater(() -> {
                    screen.setKitItems(items);
                });
                completableFuture.complete(null);
            } catch (Exception e) {
                completableFuture.completeExceptionally(e);
            }
        });
    }

    private void getPagesByIds(Messenger messenger, List<String> pageIds, List<ImageItemWrapper<Page>> items) {
        if (pageIds == null || pageIds.isEmpty()) {
            return;
        }
        try {
            List<Page> pages = messenger.getGroupsById(pageIds);
            fillItems(pages, items);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            List<Page> pages = messenger.getUsersById(pageIds);
            fillItems(pages, items);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillItems(List<Page> pages, List<ImageItemWrapper<Page>> items) {
        for (Page p : pages) {
            items.add(new ImageItemWrapper<>(p));
        }
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
