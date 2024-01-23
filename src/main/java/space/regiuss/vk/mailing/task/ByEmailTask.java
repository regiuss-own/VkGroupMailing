package space.regiuss.vk.mailing.task;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import space.regiuss.vk.mailing.enums.PageMode;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.model.PageType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class ByEmailTask extends Task<Void> {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Getter
    private final ListProperty<Page> pageListProperty = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
    private final Messenger messenger;
    private final List<String> listMail;
    private final PageMode mode;
    private LocalDateTime timeStart;
    private int currentSearchIndex;

    @Override
    protected Void call() {
        timeStart = LocalDateTime.now();
        Iterator<String> iter = listMail.iterator();
        while (iter.hasNext() && !isCancelled() && !Thread.currentThread().isInterrupted()) {
            currentSearchIndex++;
            update();
            processForSearch(iter.next());
        }
        return null;
    }


    private void update() {
        long millis = Duration.between(timeStart, LocalDateTime.now()).toMillis();
        String time = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        updateMessage(String.format(
                "Поиск %s/%-20s Время старта: %-30s Времени прошло %s",
                currentSearchIndex, listMail.size(),
                timeStart.format(DF),
                time
        ));
        updateProgress(currentSearchIndex - 1, listMail.size());
    }

    private void processForSearch(String search) {
        List<Page> pages = null;
        for (int i = 0; i < 3 && !isCancelled(); i++) {
            try {
                pages = messenger.getHints(search);
            } catch (Exception e) {
                log.warn("getHints error {}/3", i, e);
            }
        }
        if (pages == null) {
            return;
        }
        if (mode != PageMode.ALL) {
            pages.removeIf(page -> (
                    page.getType() == PageType.USER && mode == PageMode.GROUPS)
                    || (page.getType() == PageType.GROUP && mode == PageMode.USERS)
            );
        }
        if (!pages.isEmpty()) {
            List<Page> finalPages = pages;
            Platform.runLater(() -> pageListProperty.addAll(finalPages));
        }
    }
}
