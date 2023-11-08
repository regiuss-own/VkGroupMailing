package space.regiuss.vk.mailing.popup;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.regiuss.vk.mailing.model.MessageKit;
import space.regiuss.vk.mailing.node.NameCellFactory;
import space.regiuss.vk.mailing.service.MessageService;
import space.regiuss.rgfx.popup.BackgroundPopup;
import space.regiuss.rgfx.spring.RGFXAPP;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Getter
public class SelectMessagePopup extends BackgroundPopup {

    @FXML
    private ListView<MessageKit> kitList;
    @Setter
    private Consumer<MessageKit> onConfirm;
    private final MessageService messageService;

    {
        RGFXAPP.load(this, getClass().getResource("/view/popup/selectMessagePopup.fxml"));
        kitList.setCellFactory(messageKitListView -> new NameCellFactory<>());
    }

    @PostConstruct
    public void init() {
        kitList.setItems(FXCollections.observableList(messageService.getAllKits()));
    }

    @FXML
    public void onConfirm(ActionEvent event) {
        onConfirm.accept(kitList.getSelectionModel().getSelectedItem());
        onClose.run();
    }

    public void setCurrentValue(int kitId) {
        kitList.getSelectionModel().select(kitList.getItems().stream().filter(km -> km.getId().equals(kitId)).findFirst().get());
    }
}
