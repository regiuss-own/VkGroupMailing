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
import space.regiuss.vk.mailing.model.ByEmailData;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.model.PageType;
import space.regiuss.vk.mailing.model.UserInfoData;
import space.regiuss.vk.mailing.wrapper.EmailItemWrapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ByEmailTask extends Task<Void> {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Getter
    private final ListProperty<EmailItemWrapper<Page>> pageListProperty = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));

    private final ByEmailData data;
    private LocalDateTime timeStart;
    private int currentSearchIndex;

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
                "Поиск %s/%-20s Время старта: %-30s Времени прошло %s",
                currentSearchIndex, data.getListMail().size(),
                timeStart.format(DF),
                time
        ));
        updateProgress(currentSearchIndex - 1, data.getListMail().size());
    }

    private void processForSearch(String search) {

        final String searchLowerCase = search.toLowerCase(Locale.ROOT);
        List<EmailItemWrapper<Page>> pages = fetchPages(searchLowerCase);

        if (pages == null) {
            return;
        }

        filterByMode(pages);

        if (pages.isEmpty()) {
            return;
        }

        if (data.isCheckDescription()) {
            Map<PageType, List<Integer>> typeIds = pagesToTypeIds(pages);
            Map<Integer, UserInfoData> usersInfo = fetchUsersInfo(typeIds);
            Map<Integer, String> groupsInfo = fetchGroupsInfo(typeIds);
            filterByInfo(searchLowerCase, pages, usersInfo, groupsInfo);
        }

        if (pages.isEmpty()) {
            return;
        }

        Platform.runLater(() -> pageListProperty.addAll(pages));
    }

    private List<EmailItemWrapper<Page>> fetchPages(String searchLowerCase) {
        List<EmailItemWrapper<Page>> pages = null;
        for (int i = 0; i < 3 && !isCancelled(); i++) {
            try {
                pages = data.getMessenger().getHints(searchLowerCase);
                break;
            } catch (Exception e) {
                log.warn("getHints error {}/3", i, e);
            }
        }
        return pages;
    }

    private Map<PageType, List<Integer>> pagesToTypeIds(List<EmailItemWrapper<Page>> pages) {
        return pages.stream().collect(Collectors.groupingBy(
                wrapper -> wrapper.getItem().getType(),
                Collectors.mapping(wrapper -> wrapper.getItem().getId(), Collectors.toList())
        ));
    }

    private Map<Integer, UserInfoData> fetchUsersInfo(Map<PageType, List<Integer>> typeIds) {
        List<Integer> ids = typeIds.get(PageType.USER);
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        Map<Integer, UserInfoData> usersInfo = null;
        for (int i = 0; i < 3; i++) {
            try {
                usersInfo = data.getMessenger().getUserInfoByIds(ids).stream()
                        .collect(Collectors.toMap(UserInfoData::getUserId, o -> o, (t, t2) -> t));
                break;
            } catch (Exception e) {
                log.warn("getUserInfoByIds error {}/3", i, e);
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
        for (int i = 0; i < 3; i++) {
            try {
                groupsInfo = data.getMessenger().getGroupInfoByIds(ids).stream()
                        .collect(Collectors.toMap(t -> t.get("id").asInt(), o -> o.toString().toLowerCase(Locale.ROOT), (t, t2) -> t));
                break;
            } catch (Exception e) {
                log.warn("getGroupInfoByIds error {}/3", i, e);
            }
        }
        return groupsInfo;
    }

    private void filterByInfo(String searchLowerCase, List<EmailItemWrapper<Page>> pages, Map<Integer, UserInfoData> usersInfo, Map<Integer, String> groupsInfo) {
        Iterator<EmailItemWrapper<Page>> iterator = pages.iterator();
        while (iterator.hasNext()) {
            EmailItemWrapper<Page> wrapper = iterator.next();
            if (wrapper.getItem().getType() == PageType.USER) {
                if (usersInfo == null || usersInfo.isEmpty()) {
                    iterator.remove();
                    continue;
                }
                UserInfoData info = usersInfo.get(wrapper.getItem().getId());
                if (info == null || !info.getJson().toLowerCase(Locale.ROOT).contains(searchLowerCase)) {
                    iterator.remove();
                }
            } else {
                if (groupsInfo == null || groupsInfo.isEmpty()) {
                    iterator.remove();
                    continue;
                }
                String info = groupsInfo.get(wrapper.getItem().getId());
                if (info == null || !info.contains(searchLowerCase)) {
                    iterator.remove();
                }
            }
        }
    }

    private void filterByMode(List<EmailItemWrapper<Page>> pages) {
        if (data.getMode() != PageMode.ALL) {
            pages.removeIf(page -> (
                    page.getItem().getType() == PageType.USER && data.getMode() == PageMode.GROUPS)
                    || (page.getItem().getType() == PageType.GROUP && data.getMode() == PageMode.USERS)
            );
        }
    }

}
