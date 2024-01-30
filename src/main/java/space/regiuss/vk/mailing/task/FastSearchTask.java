package space.regiuss.vk.mailing.task;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import space.regiuss.vk.mailing.enums.DescriptionMode;
import space.regiuss.vk.mailing.enums.PageMode;
import space.regiuss.vk.mailing.model.*;
import space.regiuss.vk.mailing.wrapper.DescriptionItemWrapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class FastSearchTask extends Task<Void> {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Getter
    private final ListProperty<DescriptionItemWrapper<Page>> pageListProperty = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));

    private final FastSearchData data;
    private LocalDateTime timeStart;
    private int currentSearchIndex;
    private int skipCount;

    @Override
    protected Void call() {
        timeStart = LocalDateTime.now();
        Iterator<String> iter = data.getListMail().iterator();
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
                "Поиск %s/%-20s Пропущено: %-20s Время старта: %-30s Времени прошло %s",
                currentSearchIndex, data.getListMail().size(),
                skipCount,
                timeStart.format(DF),
                time
        ));
        updateProgress(currentSearchIndex - 1, data.getListMail().size());
    }

    private void processForSearch(String search) {

        final String searchLowerCase = search.toLowerCase(Locale.ROOT);
        List<Page> pages = fetchPages(searchLowerCase);

        if (pages == null) {
            return;
        }

        filterByMode(pages);

        if (pages.isEmpty()) {
            return;
        }

        List<DescriptionItemWrapper<Page>> wrappers = pages.stream()
                .map(page -> new DescriptionItemWrapper<>(page, searchLowerCase))
                .collect(Collectors.toList());

        if (data.getDescriptionMode() != DescriptionMode.NONE && !data.getDescriptionWords().isEmpty()) {
            Map<PageType, List<Integer>> typeIds = pagesToTypeIds(wrappers);
            Map<Integer, UserInfoData> usersInfo = fetchUsersInfo(typeIds);
            Map<Integer, String> groupsInfo = fetchGroupsInfo(typeIds);

            if (usersInfo == null && groupsInfo == null) {
                skipCount++;
                return;
            }

            filterByInfo(searchLowerCase, wrappers, usersInfo, groupsInfo);

            if (wrappers.isEmpty()) {
                return;
            }
        }

        List<PageId> ids = wrappers.stream().map(wrapper -> wrapper.getItem().getId()).collect(Collectors.toList());
        Set<PageId> blacklistIds = data.getPageBlacklistRepository().findAllByIdIn(ids);
        if (!blacklistIds.isEmpty()) {
            wrappers.removeIf(wrapper -> blacklistIds.contains(wrapper.getItem().getId()));
        }

        if (wrappers.isEmpty()) {
            return;
        }

        Platform.runLater(() -> pageListProperty.addAll(wrappers));
    }

    private List<Page> fetchPages(String searchLowerCase) {
        for (int i = 0; i < data.getTryCount() && !isCancelled(); i++) {
            try {
                return data.getMessenger().getHints(searchLowerCase);
            } catch (Exception e) {
                log.warn("getHints error {}/{}", i, data.getTryCount(), e);
            }
        }
        skipCount++;
        return null;
    }

    private Map<PageType, List<Integer>> pagesToTypeIds(List<DescriptionItemWrapper<Page>> wrappers) {
        return wrappers.stream().collect(Collectors.groupingBy(
                wrapper -> wrapper.getItem().getId().getPageType(),
                Collectors.mapping(wrapper -> wrapper.getItem().getId().getPageId(), Collectors.toList())
        ));
    }

    private Map<Integer, UserInfoData> fetchUsersInfo(Map<PageType, List<Integer>> typeIds) {
        List<Integer> ids = typeIds.get(PageType.USER);
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        Map<Integer, UserInfoData> usersInfo = null;
        for (int i = 0; i < data.getTryCount(); i++) {
            try {
                usersInfo = data.getMessenger().getUserInfoByIds(ids).stream()
                        .collect(Collectors.toMap(UserInfoData::getUserId, o -> o, (t, t2) -> t));
                break;
            } catch (Exception e) {
                log.warn("getUserInfoByIds error {}/{}", i, data.getTryCount(), e);
            }
        }
        return usersInfo;
    }

    private Map<Integer, String> fetchGroupsInfo(Map<PageType, List<Integer>> typeIds) {
        List<Integer> ids = typeIds.get(PageType.GROUP);
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        Map<Integer, String> groupsInfo = null;
        for (int i = 0; i < data.getTryCount(); i++) {
            try {
                groupsInfo = data.getMessenger().getGroupInfoByIds(ids).stream()
                        .collect(Collectors.toMap(t -> t.get("id").asInt(), o -> o.toString().toLowerCase(Locale.ROOT), (t, t2) -> t));
                break;
            } catch (Exception e) {
                log.warn("getGroupInfoByIds error {}/{}", i, data.getTryCount(), e);
            }
        }
        return groupsInfo;
    }

    private void filterByInfo(String searchLowerCase, List<DescriptionItemWrapper<Page>> pages, Map<Integer, UserInfoData> usersInfo, Map<Integer, String> groupsInfo) {
        Iterator<DescriptionItemWrapper<Page>> iterator = pages.iterator();
        while (iterator.hasNext()) {
            DescriptionItemWrapper<Page> wrapper = iterator.next();
            if (wrapper.getItem().getId().getPageType() == PageType.USER) {
                if (usersInfo == null || usersInfo.isEmpty()) {
                    iterator.remove();
                    continue;
                }
                UserInfoData info = usersInfo.get(wrapper.getItem().getId().getPageId());
                if (info == null) {
                    iterator.remove();
                    continue;
                }
                String description = info.getJson().toLowerCase(Locale.ROOT);
                checkDescription(searchLowerCase, iterator, description, wrapper);
            } else {
                if (groupsInfo == null || groupsInfo.isEmpty()) {
                    iterator.remove();
                    continue;
                }
                String info = groupsInfo.get(wrapper.getItem().getId().getPageId());
                if (info == null) {
                    iterator.remove();
                    continue;
                }
                checkDescription(searchLowerCase, iterator, info.toLowerCase(Locale.ROOT), wrapper);
            }
        }
    }

    private void checkDescription(String searchLowerCase, Iterator<DescriptionItemWrapper<Page>> iterator, String description, DescriptionItemWrapper<Page> wrapper) {
        List<String> found = new ArrayList<>(data.getDescriptionWords().size());
        for (String word : data.getDescriptionWords()) {
            String checkWord = word;
            if (word.equalsIgnoreCase("{search}")) {
                checkWord = searchLowerCase;
            }
            if (description.contains(checkWord)) {
                found.add(checkWord);
            }
        }
        if (data.getDescriptionMode() == DescriptionMode.ANY) {
            if (found.isEmpty()) {
                iterator.remove();
            }
        } else {
            if (found.size() != data.getDescriptionWords().size()) {
                iterator.remove();
            }
        }
        wrapper.setDescription(String.join(",", found));
    }

    private void filterByMode(List<Page> pages) {
        if (data.getPageMode() != PageMode.ALL) {
            pages.removeIf(page -> (
                    page.getId().getPageType() == PageType.USER && data.getPageMode() == PageMode.GROUPS)
                    || (page.getId().getPageType() == PageType.GROUP && data.getPageMode() == PageMode.USERS)
            );
        }
    }

}
