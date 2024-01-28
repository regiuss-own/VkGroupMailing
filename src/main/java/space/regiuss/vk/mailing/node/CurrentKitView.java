package space.regiuss.vk.mailing.node;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.regiuss.rgfx.enums.AlertVariant;
import space.regiuss.rgfx.node.SimpleAlert;
import space.regiuss.rgfx.spring.RGFXAPP;
import space.regiuss.vk.mailing.VkMailingApp;
import space.regiuss.vk.mailing.exporter.DefaultKitExporter;
import space.regiuss.vk.mailing.exporter.KitExporter;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.popup.DeleteByExceptionsPopup;
import space.regiuss.vk.mailing.screen.MailingRunnableScreen;
import space.regiuss.vk.mailing.wrapper.ImageItemWrapper;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Getter
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class CurrentKitView<T extends ImageItemWrapper<Page>> extends VBox {

    private final VkMailingApp app;

    @Setter
    private KitExporter<T> kitExporter = new DefaultKitExporter<>();

    @FXML
    private MenuButton menuButton;

    @FXML
    private Text countText;

    @FXML
    private ListView<T> listView;

    {
        RGFXAPP.load(this, getClass().getResource("/view/currentKit.fxml"));
    }

    @PostConstruct
    public void init() {
        setCellFactory(pageListView -> new PageListItem<>(getApp().getHostServices()));
        setEmptyItems();
    }

    public void setCellFactory(Callback<ListView<T>, ListCell<T>> value) {
        listView.setCellFactory(value);
    }

    public static CurrentKitView<?> getInstance() {
        return VkMailingApp.getContext().getBean(CurrentKitView.class);
    }

    @FXML
    public void clearPagesList(ActionEvent event) {
        listView.getItems().clear();
        app.showAlert(new SimpleAlert("Набор успешно очищен", AlertVariant.SUCCESS), Duration.seconds(5));
    }

    @FXML
    public void onDeleteDuplicateClick(ActionEvent event) {
        if (listView.getItems().isEmpty()) {
            app.showAlert(new SimpleAlert("Текущий набор пуст", AlertVariant.WARN), Duration.seconds(5));
            return;
        }

        Iterator<? extends ImageItemWrapper<Page>> iterator = listView.getItems().iterator();
        Set<Integer> items = new HashSet<>(listView.getItems().size());
        while (iterator.hasNext()) {
            ImageItemWrapper<Page> item = iterator.next();
            if (!items.add(item.getItem().getId().getPageId())) {
                iterator.remove();
            }
        }
        app.showAlert(new SimpleAlert("Дубликаты удалены", AlertVariant.SUCCESS), Duration.seconds(5));
    }

    @FXML
    public void onDeleteByExceptionsClick(ActionEvent event) {
        if (listView.getItems().isEmpty()) {
            app.showAlert(new SimpleAlert("Текущий набор пуст", AlertVariant.WARN), Duration.seconds(5));
            return;
        }
        DeleteByExceptionsPopup popup = new DeleteByExceptionsPopup(listView);
        popup.setOnClose(() -> app.hideModal(popup));
        popup.setOnSuccess(() -> {
            app.showAlert(new SimpleAlert("Удаление по исключениям завершено", AlertVariant.SUCCESS), Duration.seconds(5));
        });
        app.showModal(popup);
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
            kitExporter.export(listView, file, completableFuture);
        });
    }

    public void applyPageListListener(ListProperty<Page> pageListProperty, Function<Page, T> pageMapper) {
        pageListProperty.addListener((ListChangeListener<? super Page>) change -> {
            if (change.next() && change.wasAdded()) {
                List<T> items = change.getAddedSubList().stream()
                        .map(pageMapper)
                        .collect(Collectors.toList());
                getListView().getItems().addAll(items);
            }
        });
    }

    public void applyWrapperListListener(ListProperty<T> pageListProperty) {
        pageListProperty.addListener((ListChangeListener<T>) change -> {
            if (change.next() && change.wasAdded()) {
                getListView().getItems().addAll(change.getAddedSubList());
            }
        });
    }
}
