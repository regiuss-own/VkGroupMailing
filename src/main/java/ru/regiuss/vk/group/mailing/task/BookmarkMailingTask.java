package ru.regiuss.vk.group.mailing.task;

import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import ru.regiuss.vk.group.mailing.enums.BookmarkType;
import ru.regiuss.vk.group.mailing.messenger.Messenger;
import ru.regiuss.vk.group.mailing.model.BookmarkMailingData;
import ru.regiuss.vk.group.mailing.model.Fave;
import ru.regiuss.vk.group.mailing.model.Message;
import ru.regiuss.vk.group.mailing.model.User;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class BookmarkMailingTask extends Task<Void> {

    private final Messenger messenger;
    private final BookmarkMailingData data;

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
        List<Fave> faves = null;
        while (!Thread.currentThread().isInterrupted()) {
            for (int i = 0; i < 3; i++) {
                try {
                    log.info("search page {}", page);
                    faves = messenger.getFaves(page);
                    page++;
                    break;
                } catch (Exception e) {
                    log.warn("search error", e);
                }
            }
            log.info("found faves {}", faves);
            if (faves == null)
                throw new RuntimeException("Не удалось получить список групп");
            if (faves.isEmpty())
                break;
            for (Fave fave : faves) {
                if (!data.getType().equals(BookmarkType.ALL)) {
                    if (data.getType().equals(BookmarkType.USERS) && !fave.getType().equals("user"))
                        continue;
                    if (data.getType().equals(BookmarkType.GROUPS) && fave.getType().equals("user"))
                        continue;
                }
                for (Message message : data.getMessages()) {
                    for (int i = 0; i < 3; i++) {
                        try {
                            messenger.send(fave.getType().equals("user") ? fave.getId() : -fave.getId(), message);
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
