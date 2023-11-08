package ru.regiuss.vk.group.mailing.task;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import ru.regiuss.vk.group.mailing.messenger.Messenger;
import ru.regiuss.vk.group.mailing.model.ItemsResult;
import ru.regiuss.vk.group.mailing.model.Page;
import ru.regiuss.vk.group.mailing.model.ProfileTaskData;
import ru.regiuss.vk.group.mailing.model.UserInfoData;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@RequiredArgsConstructor
public class ProfileTask extends Task<Void> {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)");
    @Getter
    private final ListProperty<Page> pageListProperty = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<>()));
    private final Messenger messenger;
    private final ProfileTaskData taskData;
    private Set<String> groupIds;

    @Override
    protected Void call() throws Exception {
        String targetGroupPrepared = prepareLink(taskData.getGroup());
        groupIds = new HashSet<>(1024);
        int page = 1;
        ItemsResult<Integer> itemResult;
        do {
            for (int i = 0; true; i++) {
                try {
                    itemResult = messenger.getGroupMembers(targetGroupPrepared, page);
                    page++;
                    break;
                } catch (Exception e) {
                    if (i == 2 || isCancelled())
                        throw e;
                }
            }
            processItemResult(itemResult);
        } while (!itemResult.getItems().isEmpty() && !isCancelled());
        return null;
    }

    private void processItemResult(ItemsResult<Integer> itemResult) throws Exception {
        List<String> searchGroups = new LinkedList<>();
        List<UserInfoData> userInfos;
        for (int i = 0; true; i++) {
            try {
                userInfos = messenger.getUserInfoByIds(itemResult.getItems());
                break;
            } catch (Exception e) {
                if (i == 2 || isCancelled())
                    throw e;
            }
        }
        for (UserInfoData s : userInfos) {
            if (s.getCareer() != null) {
                for (JsonNode career : s.getCareer()) {
                    JsonNode groupIdNode = career.get("group_id");
                    if (groupIdNode != null && !groupIdNode.isNull()) {
                        String groupId = groupIdNode.asText();
                        if (groupIds.add(groupId))
                            searchGroups.add(groupId);
                    }
                }
            }
            if (s.getOccupation() != null && !s.getOccupation().isNull()) {
                JsonNode idNode = s.getOccupation().get("id");
                if (idNode != null && !idNode.isNull()) {
                    String groupId = idNode.asText();
                    if (groupIds.add(groupId))
                        searchGroups.add(groupId);
                }
            }
            Matcher matcher = URL_PATTERN.matcher(s.getJson());
            while (matcher.find()) {
                String url = matcher.group();
                if (url.contains("vk.com")) {
                    String groupId = prepareLink(url);
                    if (groupIds.add(groupId))
                        searchGroups.add(groupId);
                }
            }
        }
        if (searchGroups.isEmpty())
            return;
        List<Page> pages;
        for (int i = 0; true; i++) {
            try {
                pages = messenger.getGroupsById(searchGroups);
                break;
            } catch (Exception e) {
                if (i == 2 || isCancelled())
                    throw e;
            }
        }
        pages.removeIf(page -> (taskData.getMinSubscribersCount() > 0 && page.getSubscribers() < taskData.getMinSubscribersCount())
                || (taskData.getMaxSubscribersCount() > 0 && page.getSubscribers() > taskData.getMaxSubscribersCount()));
        List<Page> finalPages = pages;
        Platform.runLater(() -> pageListProperty.addAll(finalPages));
    }

    private String prepareLink(String url) {
        int startIndex = url.lastIndexOf('/') + 1;
        int endIndex = url.indexOf('?', startIndex);
        return url.substring(startIndex, endIndex > 0 ? endIndex : url.length());
    }
}
