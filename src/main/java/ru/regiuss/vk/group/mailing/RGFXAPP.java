package ru.regiuss.vk.group.mailing;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.prefs.Preferences;

@Getter
public abstract class RGFXAPP extends Application {

    protected Stage stage;
    protected final ExecutorService executorService = getExecutorService();

    public static void load(Object owner, URL resource) {
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setRoot(owner);
        loader.setControllerFactory(aClass -> owner);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        Preferences userPrefs = Preferences.userNodeForPackage(getClass());
        stage.setWidth(userPrefs.getDouble("app.position.w", 800));
        stage.setHeight(userPrefs.getDouble("app.position.h", 600));
        double x = userPrefs.getDouble("app.position.x", 0);
        double y = userPrefs.getDouble("app.position.y", 0);

        if(x + y > 0) {
            stage.setX(x);
            stage.setY(y);
        }
        start();
    }

    public abstract void start() throws Exception;

    public void init(int weight, int height, String title, URL icon) {
        stage.setTitle(title);
        stage.setMinWidth(weight);
        stage.setMinHeight(height);
        try (InputStream is = icon.openStream()) {
            if (is != null)
                stage.getIcons().add(new Image(is));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Preferences userPrefs = Preferences.userNodeForPackage(getClass());
        userPrefs.putDouble("app.position.x", stage.getX());
        userPrefs.putDouble("app.position.y", stage.getY());
        userPrefs.putDouble("app.position.w", stage.getWidth());
        userPrefs.putDouble("app.position.h", stage.getHeight());
        if (executorService != null)
            executorService.shutdownNow();
    }

    public abstract ExecutorService getExecutorService();
}
