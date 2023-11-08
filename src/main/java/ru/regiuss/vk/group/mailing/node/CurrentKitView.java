package ru.regiuss.vk.group.mailing.node;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.regiuss.vk.group.mailing.VkGroupApp;
import ru.regiuss.vk.group.mailing.model.ImageItemWrapper;
import ru.regiuss.vk.group.mailing.model.Page;
import ru.regiuss.vk.group.mailing.screen.MailingRunnableScreen;
import space.regiuss.rgfx.enums.AlertVariant;
import space.regiuss.rgfx.node.SimpleAlert;
import space.regiuss.rgfx.spring.RGFXAPP;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class CurrentKitView extends VBox {

    private final VkGroupApp app;

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
        return VkGroupApp.getContext().getBean(CurrentKitView.class);
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
