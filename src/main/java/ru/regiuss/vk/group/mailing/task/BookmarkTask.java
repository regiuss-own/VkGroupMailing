package ru.regiuss.vk.group.mailing.task;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.regiuss.vk.group.mailing.enums.BookmarkType;
import ru.regiuss.vk.group.mailing.messenger.Messenger;
import ru.regiuss.vk.group.mailing.model.Page;
import ru.regiuss.vk.group.mailing.model.PageType;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public class BookmarkTask extends Task<Void> {

    @Getter
    private final ListProperty<Page> pageListProperty = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<>()));
    private final Messenger messenger;
    private final BookmarkType bookmarkType;

    @Override
    protected Void call() throws Exception {
        int page = 1;
        List<Page> items;
        do {
            for (int i = 0; true; i++) {
                try {
                    items = messenger.getFaves(page);
                    page++;
                    break;
                } catch (Exception e) {
                    if (i == 2 || isCancelled())
                        throw e;
                }
            }
            if (items.isEmpty())
                break;
            switch (bookmarkType) {
                case USERS:
                    items.removeIf(item -> !item.getType().equals(PageType.USER));
                    break;
                case GROUPS:
                    items.removeIf(item -> !item.getType().equals(PageType.GROUP));
                    break;
            }
            List<Page> finalItems = items;
            Platform.runLater(() -> pageListProperty.addAll(finalItems));
        } while (!items.isEmpty() && !isCancelled());
        return null;
    }
}
