package ru.regiuss.vk.group.mailing;

import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.regiuss.vk.group.mailing.screen.GroupRunnableScreen;
import space.regiuss.rgfx.node.Loader;

@Component
@RequiredArgsConstructor
public class LoadEventListener {

    private final VkGroupApp app;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        Platform.runLater(() -> {
            app.hideModal(node -> node instanceof Loader);
            app.openScreen(GroupRunnableScreen.class);
        });
    }
}
