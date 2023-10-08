package ru.regiuss.vk.group.mailing.popup;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.Setter;

public class WarnPopup extends StackPane {

    private static FXMLLoader loader;

    @FXML
    private TextFlow contentFlow;

    @FXML
    private Text headerText;

    @Setter
    private Runnable onClose;

    public WarnPopup(String header, String message) {
        if (loader == null)
            loader = new FXMLLoader(getClass().getResource("/view/warnPopup.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
            headerText.setText(header);
            Text text = new Text(message);
            text.setFont(Font.font(16));
            contentFlow.getChildren().add(text);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onClose(ActionEvent event) {
        onClose.run();
    }

    @FXML
    void onClickBackground(MouseEvent event) {
        onClose.run();
    }
}
