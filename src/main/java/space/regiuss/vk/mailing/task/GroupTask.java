package space.regiuss.vk.mailing.task;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.model.GroupTaskResult;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.model.SearchGroupData;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GroupTask extends Task<GroupTaskResult> {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Getter
    private final ListProperty<Page> pageListProperty;
    private final Set<Integer> savedPages = new HashSet<>();
    private final Messenger messenger;
    private final SearchGroupData data;
    private int currentSearchIndex = 0;
    private final GroupTaskResult result;

    public GroupTask(Messenger messenger, SearchGroupData data) {
        this.messenger = messenger;
        this.data = data;
        this.result = new GroupTaskResult();
        this.result.setPages(new LinkedList<>());
        this.pageListProperty = new SimpleListProperty<>(FXCollections.observableList(result.getPages()));
    }

    @Override
    protected GroupTaskResult call() {
        result.setTimeStart(LocalDateTime.now());
        Iterator<String> iter = data.getSearch().iterator();
        while (iter.hasNext() && !isCancelled() && !Thread.currentThread().isInterrupted()) {
            currentSearchIndex++;
            processForSearch(iter.next());
        }
        result.setTimeEnd(LocalDateTime.now());
        return result;
    }

    private void update(int page) {
        long millis = Duration.between(result.getTimeStart(), LocalDateTime.now()).toMillis();
        String time = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        updateMessage(String.format(
                "Поиск %s/%-20sСтраница: %-20s Найдено страниц: %-20s Время старта: %-30s Времени прошло %s",
                currentSearchIndex, data.getSearch().size(),
                page, pageListProperty.size(),
                result.getTimeStart().format(DF),
                time
        ));
        updateProgress(currentSearchIndex - 1, data.getSearch().size());
    }

    private void processForSearch(String search) {
        int page = 1;
        List<Page> pages;
        boolean enabled = true;
        do {
            update(page);
            for (int i = 0; true; i++) {
                try {
                    pages = messenger.search(page, search, data.isSort());
                    page++;
                    break;
                } catch (Exception e) {
                    if(i == 2 || isCancelled()) {
                        log.warn("search error", e);
                        throw new RuntimeException("Не выполнить запрос " + e.getMessage());
                    }
                }
            }
            if (pages.isEmpty())
                break;
            Iterator<Page> iterator = pages.iterator();
            while (iterator.hasNext() && !isCancelled()) {
                Page p = iterator.next();
                if (data.isOnlyCanMessage() && !p.isCanMessage()) {
                    iterator.remove();
                    continue;
                }
                if (data.getMaxSubscribers() > 0 && data.getMaxSubscribers() < p.getSubscribers()) {
                    iterator.remove();
                    continue;
                }
                if (data.getMinSubscribers() > 0 && p.getSubscribers() < data.getMinSubscribers()) {
                    iterator.remove();
                    enabled = !data.isSort();
                    continue;
                }
                if (!savedPages.add(p.getId())) {
                    iterator.remove();
                }
            }
            if (!pages.isEmpty()) {
                List<Page> finalPages = pages;
                Platform.runLater(() -> pageListProperty.addAll(finalPages));
            }
        } while (enabled && !isCancelled());
    }
}
