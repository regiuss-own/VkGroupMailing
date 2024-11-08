package space.regiuss.vk.mailing.node;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import space.regiuss.rgfx.RGFXAPP;

import java.util.concurrent.ScheduledExecutorService;

@Component
@RequiredArgsConstructor
public class UpdateNode extends StackPane {

    private final ScheduledExecutorService executorService;

    @FXML
    private Button startDownloadButton;

    @FXML
    private HBox downloadBox;

    @FXML
    private ProgressBar downloadStatusProgressBar;

    @FXML
    private Label downloadStatusLabel;

    @FXML
    private Button startUpdateButton;

    {
        RGFXAPP.load(this, getClass().getResource("/view/updateNode.fxml"));
        getStylesheets().add(getClass().getResource("/style/updateNode.css").toExternalForm());
    }

    private void setVisible(Node node, boolean value) {
        node.setVisible(value);
        node.setManaged(value);
    }


    public void onStartDownload(ActionEvent event) {
        setVisible(startDownloadButton, false);
        setVisible(downloadBox, false);
    }

}
