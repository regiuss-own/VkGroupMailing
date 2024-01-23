package space.regiuss.vk.mailing;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import space.regiuss.version.client.VersionClient;

@SpringBootApplication
public class VkMailing {
    public static void main(String[] args) {
        VersionClient.checkCommandLineArguments(args, new AppInstaller());
        Application.launch(VkMailingApp.class, args);
    }
}
