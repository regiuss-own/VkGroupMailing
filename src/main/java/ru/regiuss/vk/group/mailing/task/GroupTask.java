package ru.regiuss.vk.group.mailing.task;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.regiuss.vk.group.mailing.messenger.Messenger;
import ru.regiuss.vk.group.mailing.model.GroupTaskResult;
import ru.regiuss.vk.group.mailing.model.Page;
import ru.regiuss.vk.group.mailing.model.SearchGroupData;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class GroupTask extends Task<GroupTaskResult> {

    @Getter
    private final ListProperty<Page> pageListProperty;
    private final Messenger messenger;
    private final SearchGroupData data;
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
        int page = 1;
        List<Page> pages;
        boolean enabled = true;
        do {
            for (int i = 0; true; i++) {
                try {
                    pages = messenger.search(page, data.getSearch(), data.isSort());
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
                if (data.getMaxSubscribers() > 0 && data.getMaxSubscribers() < p.getSubscribers()) {
                    iterator.remove();
                    continue;
                }
                if (data.getMinSubscribers() > 0 && p.getSubscribers() < data.getMinSubscribers()) {
                    iterator.remove();
                    enabled = !data.isSort();
                }
            }
            if (!pages.isEmpty()) {
                List<Page> finalPages = pages;
                Platform.runLater(() -> pageListProperty.addAll(finalPages));
            }
        } while (enabled && !isCancelled());
        result.setTimeEnd(LocalDateTime.now());
        return result;
    }
}
