package space.regiuss.vk.mailing.screen;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.regiuss.rgfx.RGFXAPP;
import space.regiuss.rgfx.enums.AlertVariant;
import space.regiuss.rgfx.node.SimpleAlert;
import space.regiuss.vk.mailing.VkMailingApp;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.wrapper.ImageItemWrapper;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class BlackListScreen extends VBox {

    private final VkMailingApp app;

    @FXML
    private ListView<ImageItemWrapper<Page>> blackListView;

    {
        RGFXAPP.load(this, getClass().getResource("/view/screen/blackListScreen.fxml"));
    }

    @FXML
    public void onAddClick(ActionEvent event) {
        app.showAlert(new SimpleAlert("В разработке", AlertVariant.DANGER), Duration.seconds(5));
    }

}
