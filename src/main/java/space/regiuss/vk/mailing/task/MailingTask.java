package space.regiuss.vk.mailing.task;

import javafx.concurrent.Task;
import javafx.scene.control.ListView;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.model.*;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class MailingTask extends Task<Void> {

    private final Messenger messenger;
    private final MailingData mailingData;
    private final ListView<?> listView;

    @Override
    protected Void call() throws Exception {
        List<ProgressItemWrapper<Page>> items = mailingData.getItems();
        for (int i = 0; i < items.size() && !isCancelled(); i++) {
            ProgressItemWrapper<Page> item = items.get(i);
            Page page = item.getItem();
            int sendCount = 0;

            for (Message message : mailingData.getMessages()) {
                for (int j = 0; j < 3; j++) {
                    try {
                        messenger.send(page.getType().equals(PageType.USER) ? page.getId() : -page.getId(), message);
                        sendCount++;
                        break;
                    } catch (RuntimeException e) {
                        log.warn("send message error", e);
                        break;
                    } catch (Exception e) {
                        log.warn("send message error", e);
                    }
                }
                if (mailingData.getMessageDelay() > 0)
                    Thread.sleep(mailingData.getMessageDelay());
            }

            item.setTotal(mailingData.getMessages().size());
            item.setProgress(sendCount);
            listView.refresh();

            if (mailingData.getDialogDelay() > 0)
                Thread.sleep(mailingData.getDialogDelay());
        }
        return null;
    }
}
