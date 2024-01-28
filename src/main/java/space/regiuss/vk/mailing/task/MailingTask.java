package space.regiuss.vk.mailing.task;

import javafx.concurrent.Task;
import javafx.scene.control.ListView;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.model.*;
import space.regiuss.vk.mailing.wrapper.ProgressItemWrapper;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class MailingTask extends Task<MailingTask.State> {

    private final Messenger messenger;
    private final MailingData mailingData;
    private final ListView<?> listView;
    private int errors = 0;

    public enum State {
        PROCESS,
        WAITING,
        FINISH
    }

    @Override
    protected MailingTask.State call() throws Exception {
        List<ProgressItemWrapper<Page>> items = mailingData.getItems();

        if (items.isEmpty()) {
            return State.FINISH;
        }

        List<PageId> ids = items.stream().map(wrapper -> wrapper.getItem().getId()).collect(Collectors.toList());
        Set<PageId> blacklistIds = mailingData.getPageBlacklistRepository().findAllByIdIn(ids);

        Iterator<ProgressItemWrapper<Page>> iterator = items.iterator();
        while (iterator.hasNext() && !isCancelled()) {
            ProgressItemWrapper<Page> item = iterator.next();
            if (blacklistIds.contains(item.getItem().getId())) {
                continue;
            }
            if (item.getProgress() != -1 && item.getTotal() != -1) {
                continue;
            }
            Page page = item.getItem();
            int sendCount = 0;

            for (Message message : mailingData.getMessages()) {
                for (int j = 0; j < 3; j++) {
                    try {
                        messenger.send(page.getId().getPageType().equals(PageType.USER) ? page.getId().getPageId() : -page.getId().getPageId(), message);
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
            updateErrors(mailingData.getMessages().size() - sendCount);
            item.setTotal(mailingData.getMessages().size());
            item.setProgress(sendCount);
            listView.refresh();

            if (mailingData.getDialogDelay() > 0)
                Thread.sleep(mailingData.getDialogDelay());
        }
        return State.FINISH;
    }

    private void updateErrors(int i) throws Exception {
        log.info("updateErrors {} + {}", errors, i);
        if (i < 1 || mailingData.getOnErrorDelay() < 1 || mailingData.getMaxErrorCount() < 1) {
            return;
        }
        errors += i;
        if (errors >= mailingData.getMaxErrorCount()) {
            log.info("start wait {} min.", mailingData.getOnErrorDelay());
            updateValue(State.WAITING);
            errors = 0;
            Thread.sleep(Duration.ofMinutes(mailingData.getOnErrorDelay()).toMillis());
            log.info("start after wait {} min.", mailingData.getOnErrorDelay());
            updateValue(State.PROCESS);
        }
    }
}
