package space.regiuss.vk.mailing;

import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import space.regiuss.vk.mailing.screen.GroupRunnableScreen;
import space.regiuss.rgfx.node.Loader;

@Component
@RequiredArgsConstructor
public class LoadEventListener {

    private final VkMailingApp app;
    @Value("${app.version}")
    private String appVersion;
    @Value("${app.name}")
    private String appName;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        Platform.runLater(() -> {
            app.getVersionText().setText(appName + " " + appVersion);
            app.hideModal(node -> node instanceof Loader);
            app.openScreen(GroupRunnableScreen.class);
        });
    }
}
