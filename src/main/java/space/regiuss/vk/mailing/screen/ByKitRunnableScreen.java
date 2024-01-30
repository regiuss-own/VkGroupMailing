package space.regiuss.vk.mailing.screen;

import javafx.fxml.FXML;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.regiuss.rgfx.RGFXAPP;
import space.regiuss.rgfx.node.RunnablePane;
import space.regiuss.vk.mailing.VkMailingApp;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.node.CurrentKitView;
import space.regiuss.vk.mailing.node.SelectAccountButton;
import space.regiuss.vk.mailing.wrapper.ImageItemWrapper;

@Log4j2
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ByKitRunnableScreen extends RunnablePane {

    private final VkMailingApp app;

    @FXML
    private SelectAccountButton selectAccountButton;

    @FXML
    @Getter
    private CurrentKitView<ImageItemWrapper<Page>> currentKitView;

    {
        RGFXAPP.load(this, getClass().getResource("/view/screen/bykit.fxml"));
    }

}
