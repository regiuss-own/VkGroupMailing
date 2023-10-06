package ru.regiuss.vk.group.mailing;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import ru.regiuss.vk.group.mailing.controller.MainController;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

@Getter
public class VkGroupApp extends Application {

    private Stage stage;
    private final ExecutorService executorService;

    public VkGroupApp() {
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            t.setName("App-Thread-Pool-");
            return t;
        });
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setTitle("VkGroupMailing");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        try (InputStream iconStream = getClass().getResourceAsStream("/img/icon.png")) {
            if (iconStream != null)
                stage.getIcons().add(new Image(iconStream));
        }

        Preferences userPrefs = Preferences.userNodeForPackage(getClass());
        stage.setWidth(userPrefs.getDouble("app.position.w", 800));
        stage.setHeight(userPrefs.getDouble("app.position.h", 600));
        double x = userPrefs.getDouble("app.position.x", 0);
        double y = userPrefs.getDouble("app.position.y", 0);

        if(x + y > 0) {
            stage.setX(x);
            stage.setY(y);
        }


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
        loader.setController(new MainController(this));
        Parent parent = loader.load();
        stage.setScene(new Scene(parent));
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Preferences userPrefs = Preferences.userNodeForPackage(getClass());
        userPrefs.putDouble("app.position.x", stage.getX());
        userPrefs.putDouble("app.position.y", stage.getY());
        userPrefs.putDouble("app.position.w", stage.getWidth());
        userPrefs.putDouble("app.position.h", stage.getHeight());
        executorService.shutdownNow();
    }
}
