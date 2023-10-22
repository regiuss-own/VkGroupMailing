package ru.regiuss.vk.group.mailing;

import javafx.scene.Parent;
import javafx.scene.Scene;
import lombok.Getter;
import ru.regiuss.vk.group.mailing.node.RootPane;
import ru.regiuss.vk.group.mailing.screen.GroupRunnableScreen;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class VkGroupApp extends RGFXAPP {

    private RootPane root;
    private final Map<String, Parent> screens = new HashMap<>(8);

    @Override
    public void start() {
        init(800, 600, "VkMailing", getClass().getResource("/img/icon.png"));
        root = new RootPane(this);
        stage.setScene(new Scene(root));
        root.getMenuToggleGroup().getToggles().get(0).setSelected(true);
        stage.show();
    }

    @Override
    public ExecutorService getExecutorService() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            t.setName("App-Thread-Pool-");
            return t;
        });
    }

    public void openGroupScreen() {
        Parent parent = screens.get("group");
        if (parent == null) {
            parent = new GroupRunnableScreen();
            screens.put("group", parent);
            openScreen(parent);
        } else {
            openScreen(parent);
        }
    }

    public void openScreen(Parent screen) {
        root.getScreen().getChildren().clear();
        root.getScreen().getChildren().add(screen);
    }

    public void showModal(Parent modal) {
        root.getChildren().add(modal);
    }

    public void hideModal(Parent modal) {
        root.getChildren().remove(modal);
    }
}
