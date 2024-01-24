package space.regiuss.vk.mailing;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import space.regiuss.version.client.VersionClient;

@SpringBootApplication
public class VkMailing {

    public static boolean showChangelog = false;

    public static void main(String[] args) {
        VersionClient.checkCommandLineArguments(args, new AppInstaller());
        for (String arg : args) {
            if (arg.equalsIgnoreCase("clearTemp")) {
                showChangelog = true;
                break;
            }
        }
        Application.launch(VkMailingApp.class, args);
    }
}
