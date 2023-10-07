package ru.regiuss.vk.group.mailing.popup;

import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.Setter;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class AuthPopup extends AnchorPane implements Initializable {
    @FXML
    private WebView webView;
    @Setter
    private Consumer<String> onToken;
    @Setter
    private Runnable onClose;

    public AuthPopup() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/authPopup.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onClose(ActionEvent event) {
        onClose.run();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        WebEngine engine = webView.getEngine();
        engine.load("https://oauth.vk.com/authorize?client_id=6121396&scope=1073737727&redirect_uri=https://oauth.vk.com/blank.html&display=page&response_type=token&revoke=1");
        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (Worker.State.SUCCEEDED.equals(newValue)) {
                String location = engine.getLocation();
                int startIndex = location.indexOf("access_token=");
                if (startIndex != -1) {
                    startIndex+=13;
                    int lastIndex = location.indexOf('&', startIndex);
                    String token = lastIndex == -1 ? location.substring(startIndex) : location.substring(startIndex, lastIndex);
                    onToken.accept(token);
                    onClose.run();
                }
            }
        });
    }
}
