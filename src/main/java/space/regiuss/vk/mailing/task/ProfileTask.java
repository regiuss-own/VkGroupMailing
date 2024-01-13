package space.regiuss.vk.mailing.task;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.model.ItemsResult;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.model.ProfileTaskData;
import space.regiuss.vk.mailing.model.UserInfoData;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        groupIds = new HashSet<>(1024);
        for (String group : taskData.getGroups()) {
            if (isCancelled())
                break;
            log.info("start get profiles from group - {}", group);
            executeGroup(group);
        }
        return null;
    }

    private void executeGroup(String group) throws Exception {
        String targetGroupPrepared = prepareLink(group);
        int page = 1;
        ItemsResult<Integer> itemResult = null;
        do {
            for (int i = 0; !isCancelled(); i++) {
                try {
                    itemResult = messenger.getGroupMembers(targetGroupPrepared, page);
                    page++;
                    break;
                } catch (Exception e) {
                    if (i == 2 || isCancelled())
                        throw e;
                }
            }
            if (itemResult == null) {
                // throw new RuntimeException("Не удалось получить информацию по пользователям");
                break;
            }
            processItemResult(itemResult);
        } while (!itemResult.getItems().isEmpty() && !isCancelled());
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
        searchGroups = searchGroups.stream().filter(s -> !(s.startsWith("app") && s.length() > 3 && Character.isDigit(s.charAt(3))))
                .map(s -> s.startsWith("id") ? s.substring(2) : s).collect(Collectors.toList());
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
        Iterator<Page> iterator = pages.iterator();
        while (iterator.hasNext()) {
            Page p = iterator.next();
            if (taskData.isOnlyCanMessage() && !p.isCanMessage()) {
                iterator.remove();
                continue;
            }
            if (
                    (taskData.getMinSubscribersCount() > 0 && p.getSubscribers() < taskData.getMinSubscribersCount())
                || (taskData.getMaxSubscribersCount() > 0 && p.getSubscribers() > taskData.getMaxSubscribersCount())
            ) {
                iterator.remove();
            }
        }
        List<Page> finalPages = pages;
        Platform.runLater(() -> pageListProperty.addAll(finalPages));
    }

    private String prepareLink(String url) {
        int startIndex = url.lastIndexOf('/') + 1;
        int endIndex = url.indexOf('?', startIndex);
        return url.substring(startIndex, endIndex > 0 ? endIndex : url.length());
    }
}
