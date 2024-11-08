package space.regiuss.vk.mailing;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VkMailing {

    public static boolean showChangelog = false;

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("clearTemp")) {
                showChangelog = true;
                break;
            }
        }
        Application.launch(VkMailingApp.class, args);
    }

}
