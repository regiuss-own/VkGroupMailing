package space.regiuss.vk.mailing.task;

import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import space.regiuss.vk.mailing.enums.BookmarkType;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.model.*;

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
        List<Page> faves = null;
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
            for (Page fave : faves) {
                if (!data.getType().equals(BookmarkType.ALL)) {
                    if (data.getType().equals(BookmarkType.USERS) && !fave.getType().equals(PageType.USER))
                        continue;
                    if (data.getType().equals(BookmarkType.GROUPS) && fave.getType().equals(PageType.USER))
                        continue;
                }
                for (Message message : data.getMessages()) {
                    for (int i = 0; i < 3; i++) {
                        try {
                            messenger.send(fave.getType().equals(PageType.USER) ? fave.getId() : -fave.getId(), message);
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
