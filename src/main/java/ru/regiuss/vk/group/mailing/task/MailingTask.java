package ru.regiuss.vk.group.mailing.task;

import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ru.regiuss.vk.group.mailing.messenger.Messenger;
import ru.regiuss.vk.group.mailing.model.Group;
import ru.regiuss.vk.group.mailing.model.MailingData;
import ru.regiuss.vk.group.mailing.model.Message;
import ru.regiuss.vk.group.mailing.model.User;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class MailingTask extends Task<Void> {

    private final Messenger messenger;
    private final MailingData data;
    @Setter
    private boolean enabled = true;

    @Override
    protected Void call() throws Exception {
        long timeStart = System.currentTimeMillis();
        int sendCount = 0;

        updateMessage(timeStart, sendCount);

        User user = null;
        for (int i = 0; i < 3; i++) {
            try {
                user = messenger.getUser();
                break;
            } catch (Exception e) {
                log.warn("get user error", e);
            }
        }
        log.info("auth user {}", user);
        if (user == null)
            throw new RuntimeException("Токен недействителен");
        int page = 1;
        List<Group> groups = null;
        while (enabled && !Thread.currentThread().isInterrupted()) {
            for (int i = 0; i < 3; i++) {
                try {
                    log.info("search page {}", page);
                    groups = messenger.search(page, data.getSearch());
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
            for (Group group : groups) {
                if (data.getMinSubscribers() > 0 && group.getSubscribers() < data.getMinSubscribers()) {
                    enabled = false;
                    break;
                }
                for (Message message : data.getMessages()) {
                    for (int i = 0; i < 3; i++) {
                        try {
                            //messenger.send(group.getId(), message);
                            updateMessage(timeStart, ++sendCount);
                            break;
                        } catch (Exception e) {
                            log.warn("send message error", e);
                        }
                    }
                    if (data.getMessageDelay() > 0)
                        Thread.sleep(data.getMessageDelay());
                }
                if (data.getGroupDelay() > 0)
                    Thread.sleep(data.getGroupDelay());
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
