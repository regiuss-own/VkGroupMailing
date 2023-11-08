package ru.regiuss.vk.group.mailing.screen;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.regiuss.vk.group.mailing.MessageListCell;
import ru.regiuss.vk.group.mailing.VkGroupApp;
import ru.regiuss.vk.group.mailing.model.Message;
import ru.regiuss.vk.group.mailing.model.MessageKit;
import ru.regiuss.vk.group.mailing.node.RemoveNameCellFactory;
import ru.regiuss.vk.group.mailing.popup.MessagePopup;
import ru.regiuss.vk.group.mailing.service.MessageService;
import space.regiuss.rgfx.RGFXAPP;
import space.regiuss.rgfx.enums.AlertVariant;
import space.regiuss.rgfx.node.SimpleAlert;
import space.regiuss.rgfx.popup.ConfirmPopup;

import javax.annotation.PostConstruct;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class MessagesScreen extends HBox {

    private final MessageService messageService;
    private final VkGroupApp app;

    @FXML
    private VBox messageKitBox;

    @FXML
    private TextField nameField;

    @FXML
    private ListView<MessageKit> kitList;

    @FXML
    private ListView<Message> messagesList;

    {
        RGFXAPP.load(this, getClass().getResource("/view/screen/messages.fxml"));
        messageKitBox.visibleProperty().bind(Bindings.isNotNull(kitList.getSelectionModel().selectedItemProperty()));
    }

    @PostConstruct
    public void init() {
        kitList.setCellFactory(messageListView -> {
            RemoveNameCellFactory<MessageKit> cell = new RemoveNameCellFactory<>();
            cell.setRemoveConsumer(kit -> {
                ConfirmPopup popup = new ConfirmPopup(
                        "Подтверждение",
                        "Вы действительно хотите удалить набор " + kit.getName() + "?"
                );
                popup.setOnClose(() -> app.hideModal(popup));
                popup.setOnConfirm(() -> {
                    messageService.delete(kit);
                    kitList.getSelectionModel().clearSelection();
                    kitList.getItems().remove(kit);
                    app.showAlert(new SimpleAlert(
                            "Набор сообщений " + kit.getName() + " успешно удален", AlertVariant.SUCCESS
                    ), Duration.seconds(5));
                });
                app.showModal(popup);
            });
            return cell;
        });
        kitList.setOnKeyPressed(keyEvent -> {
            if (KeyCode.ESCAPE.equals(keyEvent.getCode()))
                kitList.getSelectionModel().clearSelection();
        });
        kitList.getItems().addAll(messageService.getAllKits());
        kitList.getSelectionModel().selectedItemProperty()
                .addListener((observableValue, nameProvider, t1) -> {
                    if (t1 == null)
                        return;
                    nameField.setText(t1.getName());
                    messagesList.setItems(FXCollections.observableList(t1.getMessages()));
                });
        if (!kitList.getItems().isEmpty())
            kitList.getSelectionModel().select(0);
        messagesList.setCellFactory(messageListView -> new MessageListCell());
        messagesList.setOnKeyPressed(keyEvent -> {
            if (KeyCode.ESCAPE.equals(keyEvent.getCode()))
                messagesList.getSelectionModel().clearSelection();
        });
        messagesList.setOnMouseClicked(event -> {
            if(MouseButton.PRIMARY.equals(event.getButton()) && event.getClickCount() == 2) {
                Message msg = messagesList.getSelectionModel().getSelectedItem();
                if (msg == null)
                    return;
                MessagePopup popup = new MessagePopup(app);
                popup.setOnClose(() -> app.hideModal(popup));
                popup.setOnMessage(message -> {
                    messageService.save(message);
                    messagesList.refresh();
                });
                popup.setMessage(msg);
                app.showModal(popup);
            }
        });
    }

    @FXML
    public void onAddMessageKitClick(ActionEvent event) {
        MessageKit messageKit = new MessageKit();
        messageKit.setName("Без имени");
        messageService.save(messageKit);
        kitList.getItems().add(messageKit);
        kitList.scrollTo(messageKit);
        kitList.getSelectionModel().select(messageKit);
    }

    @FXML
    public void onMessageKitSaveNameClick(ActionEvent event) {
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            app.showAlert(new SimpleAlert("Имя набора не может быть пустым", AlertVariant.DANGER), Duration.seconds(5));
            return;
        }
        MessageKit kit = kitList.getSelectionModel().getSelectedItem();
        if (kit == null)
            return;
        if (name.equals(kit.getName()))
            return;
        messageService.editKitName(kit, name);
        app.showAlert(new SimpleAlert("Имя набора изменено", AlertVariant.SUCCESS), Duration.seconds(5));
        kitList.refresh();
    }

    @FXML
    public void onAddMessageClick(ActionEvent event) {
        MessagePopup popup = new MessagePopup(app);
        popup.setOnClose(() -> app.hideModal(popup));
        popup.setOnMessage(message -> {
            if (message == null)
                return;
            MessageKit kit = kitList.getSelectionModel().getSelectedItem();
            if (kit == null)
                return;
            //kit.getMessages().add(message);
            messageService.save(message);
            messagesList.getItems().add(message);
            messageService.save(kit);
            //messagesList.refresh();
            //message.setMessageKit(kit);
            //messageService.save(message);
            //messagesList.getItems().add(message);
        });
        app.showModal(popup);
    }

    @FXML
    public void onRemoveMessageClick(ActionEvent event) {
        Message message = messagesList.getSelectionModel().getSelectedItem();
        if (message == null) {
            app.showAlert(new SimpleAlert("Выберите сообщение для удаления", AlertVariant.DANGER), Duration.seconds(5));
            return;
        }
        MessageKit kit = kitList.getSelectionModel().getSelectedItem();
        if (kit == null)
            return;
        messagesList.getItems().remove(message);
        messageService.save(kit);
    }
}
