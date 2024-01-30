package space.regiuss.vk.mailing.util;

import javafx.scene.control.TextField;
import javafx.util.Duration;
import lombok.extern.log4j.Log4j2;
import space.regiuss.rgfx.enums.AlertVariant;
import space.regiuss.rgfx.node.SimpleAlert;
import space.regiuss.vk.mailing.VkMailingApp;

@Log4j2
public class Utils {

    public static int parseNumber(
            TextField field,
            String fieldDisplayName,
            VkMailingApp app,
            int defaultValue
    ) {
        try {
            return Integer.parseInt(field.getText());
        } catch (Exception e) {
            log.warn("number convert error", e);
            app.showAlert(
                    new SimpleAlert(
                            String.format(
                                    "Неверный формат поля %s\nиспользовано значение по умолчанию - %s",
                                    fieldDisplayName, defaultValue
                            ),
                            AlertVariant.WARN
                    ),
                    Duration.seconds(5)
            );
        }
        return 0;
    }

}
