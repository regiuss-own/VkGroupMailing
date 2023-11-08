package space.regiuss.vk.mailing.task;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import space.regiuss.vk.mailing.enums.BookmarkType;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.model.PageType;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public class BookmarkTask extends Task<Void> {

    @Getter
    private final ListProperty<Page> pageListProperty = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<>()));
    private final Messenger messenger;
    private final BookmarkType bookmarkType;
    private final boolean onlyCanMessage;

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
            Iterator<Page> iterator = items.iterator();
            while (iterator.hasNext()) {
                Page p = iterator.next();
                if (onlyCanMessage && !p.isCanMessage()) {
                    iterator.remove();
                    continue;
                }
                if (!BookmarkType.ALL.equals(bookmarkType)) {
                    if (
                            (BookmarkType.USERS.equals(bookmarkType) && !PageType.USER.equals(p.getType()))
                            || (BookmarkType.GROUPS.equals(bookmarkType) && !PageType.GROUP.equals(p.getType()))
                    ) {
                        iterator.remove();
                    }
                }
            }
            List<Page> finalItems = items;
            Platform.runLater(() -> pageListProperty.addAll(finalItems));
        } while (!items.isEmpty() && !isCancelled());
        return null;
    }
}
