package space.regiuss.vk.mailing.task;

import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.model.PageId;
import space.regiuss.vk.mailing.model.PageType;
import space.regiuss.vk.mailing.wrapper.ImageItemWrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class ImportTask extends Task<List<ImageItemWrapper<Page>>> {

    private final Messenger messenger;
    private final File importFile;

    @Override
    protected List<ImageItemWrapper<Page>> call() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(importFile))) {
            List<ImageItemWrapper<Page>> items = new LinkedList<>();
            reader.readLine();
            String line;
            List<String> pageIds = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] data = line.trim().split(";");
                if (data.length < 6 && data.length > 1) {
                    log.warn("import format error {}", line );
                    continue;
                }
                if (data.length == 1) {
                    String pageId = data[0];
                    int index = pageId.lastIndexOf('/');
                    if (index != -1) {
                        pageId = pageId.substring(index + 1);
                    }
                    index = pageId.indexOf('?');
                    if (index != -1) {
                        pageId = pageId.substring(0, index);
                    }
                    pageIds.add(pageId);
                    if (pageIds.size() >= 10) {
                        getPagesByIds(pageIds, items);
                    }
                } else {
                    Page page = new Page();
                    page.setId(new PageId(
                            Integer.parseInt(data[1]),
                            PageType.valueOf(data[2])
                    ));
                    page.setName(data[3]);
                    page.setSubscribers(Integer.parseInt(data[4]));
                    page.setIcon(data[5]);
                    ImageItemWrapper<Page> item = new ImageItemWrapper<>(page);
                    items.add(item);
                }
            }
            getPagesByIds(pageIds, items);
            if (items.isEmpty())
                throw new RuntimeException("Файл пуст");

            return items;
        }
    }

    private void getPagesByIds(List<String> pageIds, List<ImageItemWrapper<Page>> items) {
        if (pageIds == null || pageIds.isEmpty()) {
            return;
        }
        try {
            List<Page> pages = messenger.getGroupsById(pageIds);
            fillItems(pages, items);
        } catch (Exception ignored) {}
        try {
            List<Page> pages = messenger.getUsersById(pageIds);
            fillItems(pages, items);
        } catch (Exception ignored) {}
    }

    private void fillItems(List<Page> pages, List<ImageItemWrapper<Page>> items) {
        for (Page p : pages) {
            items.add(new ImageItemWrapper<>(p));
        }
    }

}
