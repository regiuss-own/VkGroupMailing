package space.regiuss.vk.mailing;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import space.regiuss.rgfx.node.Loader;
import space.regiuss.rgfx.node.RootSideBarPane;
import space.regiuss.vk.mailing.node.UpdateNode;
import space.regiuss.vk.mailing.popup.ChangelogPopup;
import space.regiuss.vk.mailing.screen.GroupRunnableScreen;
import space.regiuss.vk.mailing.task.CheckUpdateTask;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class LoadEventListener {

    private final VkMailingApp app;
    private final CheckUpdateTask checkUpdateTask;
    private final UpdateNode updateNode;
    @Value("${app.version}")
    private final String appVersion;
    @Value("${app.name}")
    private final String appName;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("START {} version {}", appName, appVersion);
        app.getExecutorService().scheduleAtFixedRate(checkUpdateTask, 0, 5, TimeUnit.MINUTES);
        Platform.runLater(() -> {
            RootSideBarPane root = (RootSideBarPane) app.getRoot();

            Hyperlink versionText = new Hyperlink(appName + " " + appVersion);
            versionText.setTextFill(Paint.valueOf("gray"));
            versionText.setStyle("-fx-text-fill: gray; -fx-fill: gray;");
            versionText.setOnAction(event -> {
                versionText.setVisited(false);
                ChangelogPopup popup = new ChangelogPopup();
                popup.setOnClose(() -> app.hideModal(popup));
                app.showModal(popup);
            });

            VBox footer = new VBox(updateNode, versionText);
            footer.setAlignment(Pos.CENTER);
            footer.setFillWidth(true);
            footer.setSpacing(10);

            root.setFooter(footer);

            app.hideModal(node -> node instanceof Loader);
            app.openScreen(GroupRunnableScreen.class);
        });
    }
}
