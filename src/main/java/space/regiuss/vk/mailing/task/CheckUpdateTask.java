package space.regiuss.vk.mailing.task;

import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import space.regiuss.vk.mailing.MailingVersionClient;
import space.regiuss.vk.mailing.node.UpdateNode;

@Component
@RequiredArgsConstructor
public class CheckUpdateTask implements Runnable {

    private final UpdateNode updateNode;
    private final MailingVersionClient client;

    @Override
    public void run() {
        if (client.checkNeedUpdate()) {
            Platform.runLater(() -> {
                updateNode.setVisible(true);
                updateNode.setManaged(true);
            });
        }
    }
}
