package space.regiuss.vk.mailing.task;

import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.model.Account;
import space.regiuss.vk.mailing.model.Message;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.model.SearchGroupData;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class SearchMailingTask extends Task<Void> {

    private final Messenger messenger;
    private final SearchGroupData data;
    @Setter
    private boolean enabled = true;

    @Override
    protected Void call() throws Exception {
        long timeStart = System.currentTimeMillis();
        int sendCount = 0;

        updateMessage(timeStart, sendCount);

        Account user = null;
        for (int i = 0; i < 3; i++) {
            try {
                user = messenger.getAccount();
                break;
            } catch (Exception e) {
                log.warn("get user error", e);
            }
        }
        log.info("auth user {}", user);
        if (user == null)
            throw new RuntimeException("Токен недействителен");
        int page = 1;
        List<Page> groups = null;
        while (enabled && !Thread.currentThread().isInterrupted()) {
            for (int i = 0; i < 3; i++) {
                try {
                    log.info("search page {}", page);
                    groups = messenger.search(page, data.getSearch(), data.isSort());
                    page++;
                    break;
                } catch (Exception e) {
                    log.warn("search error", e);
                }
            }
            log.info("found groups {}", groups);
            if (groups == null)
                throw new RuntimeException("Не удалось получить список групп");
            if (groups.isEmpty())
                break;
            for (Page group : groups) {
                if (data.getMaxSubscribers() > 0 && data.getMaxSubscribers() < group.getSubscribers())
                    continue;
                if (
                        data.getMinSubscribers() > 0 && group.getSubscribers() < data.getMinSubscribers()
                ) {
                    if (data.isSort()) {
                        enabled = false;
                        break;
                    } else
                        continue;
                }
                for (Message message : data.getMessages()) {
                    for (int i = 0; i < 3; i++) {
                        try {
                            messenger.send(-group.getId(), message);
                            sendCount++;
                            break;
                        } catch (Exception e) {
                            log.warn("send message error", e);
                        }
                    }
                    updateMessage(timeStart, sendCount);
                    if (data.getMessageDelay() > 0)
                        Thread.sleep(data.getMessageDelay());
                }
                if (data.getDialogDelay() > 0)
                    Thread.sleep(data.getDialogDelay());
            }
        }
        return null;
    }

    private void updateMessage(long timeStart, int sendCount) {
        long seconds = (System.currentTimeMillis() - timeStart) / 1000;
        int HH = (int) (seconds / 3600);
        int MM = (int) ((seconds % 3600) / 60);
        int SS = (int) (seconds % 60);
        updateMessage(String.format(
                "Состояние: Запущено      Сообщений отправлено: %s    Прошло времени: %02d:%02d:%02d",
                sendCount,
                HH, MM, SS
        ));
    }
}
