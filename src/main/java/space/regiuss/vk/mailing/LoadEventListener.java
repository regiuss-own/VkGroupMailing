package space.regiuss.vk.mailing;

import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import space.regiuss.vk.mailing.screen.GroupRunnableScreen;
import space.regiuss.rgfx.node.Loader;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class LoadEventListener {

    private final VkMailingApp app;
    @Value("${app.version}")
    private String appVersion;
    @Value("${app.name}")
    private String appName;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("START {} version {}", appName, appVersion);
        Platform.runLater(() -> {
            app.getVersionText().setText(appName + " " + appVersion);
            app.hideModal(node -> node instanceof Loader);
            app.openScreen(GroupRunnableScreen.class);
        });
    }
}
