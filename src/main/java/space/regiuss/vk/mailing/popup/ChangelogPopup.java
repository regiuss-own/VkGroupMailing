package space.regiuss.vk.mailing.popup;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import lombok.RequiredArgsConstructor;
import space.regiuss.rgfx.RGFXAPP;
import space.regiuss.rgfx.popup.BackgroundPopup;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@RequiredArgsConstructor
public class ChangelogPopup extends BackgroundPopup {

    @FXML
    private TextFlow textFlow;

    {
        RGFXAPP.load(this, getClass().getResource("/view/popup/changelogPopup.fxml"));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/changelog")))) {
            StringBuilder sb = new StringBuilder();
            Font lineFont = new Font(16);
            Font versionFont = new Font(20);
            for (String line; (line = reader.readLine()) != null; ) {
                if (line.trim().isEmpty()) {
                    sb.append('\n');
                } else if (line.charAt(0) == '-') {
                    sb.append(line).append('\n');
                } else {
                    writeLines(sb, lineFont);
                    sb = new StringBuilder();
                    Text versionLine = new Text(line + "\n");
                    versionLine.setFont(versionFont);
                    textFlow.getChildren().add(versionLine);
                }
            }
            writeLines(sb, lineFont);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeLines(StringBuilder sb, Font lineFont) {
        if (sb.length() > 0) {
            Text textLine = new Text(sb.toString());
            textLine.setFont(lineFont);
            textFlow.getChildren().add(textLine);
        }
    }

    @FXML
    public void onClose(ActionEvent event) {
        onClose.run();
    }

}
