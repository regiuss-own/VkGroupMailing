package space.regiuss.vk.mailing.task;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import space.regiuss.vk.mailing.enums.PageMode;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.model.PageId;
import space.regiuss.vk.mailing.repository.PageBlacklistRepository;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BookmarkTask extends Task<Void> {

    @Getter
    private final ListProperty<Page> pageListProperty = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<>()));
    private final Messenger messenger;
    private final PageMode bookmarkType;
    private final PageBlacklistRepository pageBlacklistRepository;
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
                if (!PageMode.ALL.equals(bookmarkType)) {
                    if (
                            (PageMode.USERS.equals(bookmarkType) && !space.regiuss.vk.mailing.model.PageType.USER.equals(p.getId().getPageType()))
                            || (PageMode.GROUPS.equals(bookmarkType) && !space.regiuss.vk.mailing.model.PageType.GROUP.equals(p.getId().getPageType()))
                    ) {
                        iterator.remove();
                    }
                }
            }

            if (items.isEmpty()) {
                continue;
            }

            List<PageId> ids = items.stream().map(Page::getId).collect(Collectors.toList());
            Set<PageId> blacklistIds = pageBlacklistRepository.findAllByIdIn(ids);
            items.removeIf(p -> blacklistIds.contains(p.getId()));

            if (items.isEmpty()) {
                continue;
            }

            List<Page> finalItems = items;
            Platform.runLater(() -> pageListProperty.addAll(finalItems));
        } while (!items.isEmpty() && !isCancelled());
        return null;
    }
}
