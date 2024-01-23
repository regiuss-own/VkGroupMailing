package space.regiuss.vk.mailing.node;

import javafx.application.Platform;
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
import space.regiuss.version.client.model.DownloadStatus;
import space.regiuss.vk.mailing.MailingVersionClient;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class UpdateNode extends StackPane {

    private final MailingVersionClient client;
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
        setVisible(downloadBox, true);
        Consumer<DownloadStatus> downloadStatusConsumer = downloadStatus -> Platform.runLater(() -> {
            double progress = downloadStatus.getLength() / (double) downloadStatus.getContentSize();
            downloadStatusLabel.setText(String.format("Скачивание: %s%%", (int) (progress * 100)));
            downloadStatusProgressBar.setProgress(progress);
        });
        executorService.execute(() -> {
            File downloadFile = client.download(downloadStatusConsumer);
            Platform.runLater(() -> {
                startUpdateButton.setOnAction(e -> {
                    setVisible(startUpdateButton, false);
                    client.startInstall(downloadFile);
                    Platform.exit();
                });
                setVisible(downloadBox, false);
                setVisible(startUpdateButton, true);
            });
        });
    }
}
