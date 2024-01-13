package space.regiuss.vk.mailing.node;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.regiuss.rgfx.enums.AlertVariant;
import space.regiuss.rgfx.node.SimpleAlert;
import space.regiuss.rgfx.spring.RGFXAPP;
import space.regiuss.vk.mailing.VkMailingApp;
import space.regiuss.vk.mailing.model.ImageItemWrapper;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.screen.MailingRunnableScreen;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Getter
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class CurrentKitView extends VBox {

    private final VkMailingApp app;

    @FXML
    private Text countText;

    @FXML
    private ListView<ImageItemWrapper<Page>> listView;

    {
        RGFXAPP.load(this, getClass().getResource("/view/currentKit.fxml"));
    }

    @PostConstruct
    public void init() {
        listView.setCellFactory(pageListView -> new PageListItem<>(getApp().getHostServices()));
        setEmptyItems();
    }

    public static CurrentKitView getInstance() {
        return VkMailingApp.getContext().getBean(CurrentKitView.class);
    }

    @FXML
    public void clearPagesList(ActionEvent event) {
        listView.getItems().clear();
        app.showAlert(new SimpleAlert("Набор успешно очищен", AlertVariant.SUCCESS), Duration.seconds(5));
    }

    @FXML
    public void onMailingClick(ActionEvent event) {
        if (listView.getItems().isEmpty()) {
            app.showAlert(new SimpleAlert("Текущий набор пуст", AlertVariant.WARN), Duration.seconds(5));
            return;
        }
        MailingRunnableScreen screen = app.getScreen(MailingRunnableScreen.class);
        if (screen.setKitItems(listView.getItems())) {
            setEmptyItems();
            app.openScreen(screen);
            app.showAlert(new SimpleAlert("Набор успешно загружен для рассылки", AlertVariant.SUCCESS), Duration.seconds(5));
        }
    }

    private void setEmptyItems() {
        listView.setItems(FXCollections.observableList(new LinkedList<>()));
        countText.visibleProperty().unbind();
        countText.textProperty().unbind();
        countText.visibleProperty().bind(Bindings.isEmpty(listView.getItems()).not());
        countText.textProperty().bind(Bindings.size(listView.getItems()).asString("(%s)"));
    }

    @FXML
    public void onOpenKitClick(ActionEvent event) {
        //app.openScreen();
    }

    @FXML
    public void onOpenMailingClick(ActionEvent event) {
        app.openScreen(MailingRunnableScreen.class);
    }

    @FXML
    public void onExportClick(ActionEvent event) {
        if (listView.getItems().isEmpty()) {
            app.showAlert(new SimpleAlert("Текущий набор пуст", AlertVariant.WARN), Duration.seconds(5));
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("csv", "*.csv"));
        File file = chooser.showSaveDialog(app.getStage());
        if (file == null)
            return;
        Button button = (Button) event.getTarget();
        button.setDisable(true);
        app.showAlert(new SimpleAlert("Начинаю экспорт набора", AlertVariant.SUCCESS), Duration.seconds(5));
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.whenComplete((r, e) -> Platform.runLater(() -> {
            button.setDisable(false);
            if (e == null) {
                app.showAlert(new SimpleAlert("Экспорт набора завершен", AlertVariant.SUCCESS), Duration.seconds(5));
            } else {
                log.warn("export error", e);
                app.showAlert(new SimpleAlert("Не удалось экспортировать набор: " + e.getMessage(), AlertVariant.DANGER), Duration.seconds(5));
            }
        }));
        app.getExecutorService().execute(() -> {
            try (OutputStream os = new FileOutputStream(file)) {
                os.write(239);
                os.write(187);
                os.write(191);
                os.write("ссылка;id;тип;имя;подписчики;фото\n".getBytes(StandardCharsets.UTF_8));
                for (ImageItemWrapper<Page> item : listView.getItems()) {
                    Page page = item.getItem();
                    os.write(String.format(
                            "%s;%s;%s;%s;%s;%s%n",
                            page.getLink(),
                            page.getId(),
                            page.getType().name(),
                            page.getName(),
                            page.getSubscribers(),
                            page.getIcon()
                    ).getBytes(StandardCharsets.UTF_8));
                }
                completableFuture.complete(null);
            } catch (Exception e) {
                completableFuture.completeExceptionally(e);
            }
        });
    }

    public void applyPageListListener(ListProperty<Page> pageListProperty) {
        pageListProperty.addListener((ListChangeListener<? super Page>) change -> {
            if (change.next() && change.wasAdded()) {
                List<ImageItemWrapper<Page>> items = change.getAddedSubList().stream()
                        .map((Function<Page, ImageItemWrapper<Page>>) ImageItemWrapper::new)
                        .collect(Collectors.toList());
                getListView().getItems().addAll(items);
            }
        });
    }
}
