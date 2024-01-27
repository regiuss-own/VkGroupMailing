package space.regiuss.vk.mailing.messenger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import space.regiuss.vk.mailing.model.*;
import space.regiuss.vk.mailing.wrapper.EmailItemWrapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class VkMessenger implements Messenger {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final String VERSION = "5.199";
    private static final String BASE_PATH = "https://api.vk.com";
    private final Map<String, String> files;
    private final String token;

    public VkMessenger(String token) {
        this.token = token;
        this.files = new HashMap<>(20);
    }

    @Override
    public List<Page> search(int page, String search, boolean sort) throws Exception {
        try (InputStream is = executeByToken(
                "/method/groups.search",
                "q", search,
                "sort", sort ? 6 : 0,
                "offset", 10 * (page - 1),
                "count", 10,
                "fields", "members_count, can_message"
        )) {
            return OM.readValue(is, new TypeReference<Response<ItemsResult<JsonNode>>>() {
                    }).getResponse().getItems()
                    .stream().map(this::groupNodeToPage).collect(Collectors.toList());
        }
    }

    private Page groupNodeToPage(JsonNode node) {
        Page p = new Page();
        p.setType(PageType.GROUP);
        p.setId(node.get("id").asInt());
        p.setSubscribers(node.get("members_count").asInt());
        p.setIcon(node.get("photo_100").asText());
        p.setName(node.get("name").asText());
        p.setCanMessage(node.get("can_message").asInt() == 1);
        return p;
    }

    private Page userNodeToPage(JsonNode node) {
        Page p = new Page();
        p.setType(PageType.USER);
        p.setId(node.get("id").asInt());
        if (node.hasNonNull("followers_count")) {
            p.setSubscribers(node.get("followers_count").asInt());
        }
        p.setIcon(node.get("photo_100").asText());
        p.setName(node.get("first_name").asText() + " " + node.get("last_name").asText());
        p.setCanMessage(node.get("can_write_private_message").asInt() == 1);
        return p;
    }

    @Override
    public List<Page> getFaves(int page) throws Exception {
        try (InputStream is = executeByToken(
                "/method/fave.getPages",
                "offset", 10 * (page - 1),
                "fields", "members_count, photo_100, can_message, can_write_private_message, followers_count",
                "count", 10
        )) {
            return OM.readValue(is, new TypeReference<Response<ItemsResult<JsonNode>>>() {
                    }).getResponse().getItems()
                    .stream().map(node -> {
                        String type = node.get("type").asText();
                        JsonNode n = node.get(type);
                        Page pageData = new Page();
                        pageData.setType(PageType.valueOf(type.toUpperCase(Locale.ROOT)));
                        pageData.setId(n.get("id").asInt());
                        pageData.setIcon(n.get("photo_100").asText());
                        if (type.equals("user")) {
                            pageData.setName(n.get("first_name").asText() + " " + n.get("last_name").asText());
                            pageData.setCanMessage(n.get("can_write_private_message").asInt() == 1);
                            pageData.setSubscribers(n.get("followers_count").asInt());
                        } else {
                            pageData.setName(n.get("name").asText());
                            pageData.setSubscribers(n.get("members_count").asInt());
                            pageData.setCanMessage(n.get("can_message").asInt() == 1);
                        }
                        return pageData;
                    }).collect(Collectors.toList());
        }
    }

    @Override
    public ItemsResult<Integer> getGroupMembers(String group, int page) throws Exception {
        try (InputStream is = executeByToken(
                "/method/groups.getMembers",
                "offset", 100 * (page - 1),
                "group_id", group,
                "count", 100
        ); BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
            String response = sb.toString();
            log.debug("getGroupMembers response {}", response);
            return OM.readValue(response, new TypeReference<Response<ItemsResult<Integer>>>() {
            }).getResponse();
        }
    }

    @Override
    public List<UserInfoData> getUserInfoByIds(List<Integer> userIds) throws Exception {
        try (InputStream is = executeByToken(
                "/method/users.get",
                "user_ids", userIds.stream().map(id -> Integer.toString(id)).collect(Collectors.joining(",")),
                "fields", "activities,about,can_message,books,career,connections,contacts,domain,education,exports,site,status,interests,military,movies,music,occupation,quotes,tv"
        ); InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return OM.readValue(reader, new TypeReference<Response<List<JsonNode>>>() {
                    }).getResponse()
                    .stream().map(node -> {
                        UserInfoData data = new UserInfoData();
                        data.setUserId(node.get("id").asInt());
                        data.setCareer(node.get("career"));
                        data.setOccupation(node.get("occupation"));
                        data.setJson(node.toString());
                        return data;
                    }).collect(Collectors.toList());
        }
    }

    @Override
    public List<Page> getGroupsById(Collection<String> groups) throws Exception {
        log.debug("getGroupsById groups: {}", groups);
        try (InputStream is = executeByToken(
                "/method/groups.getById",
                "group_ids", String.join(",", groups),
                "fields", "members_count,can_message"
        ); BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
            String response = sb.toString();
            log.debug("getGroupsById response {}", response);
            JsonNode groupsNode = OM.readValue(response, new TypeReference<Response<JsonNode>>() {})
                    .getResponse().get("groups");
            List<Page> pages = new LinkedList<>();
            for (JsonNode groupNode : groupsNode) {
                if (groupNode.hasNonNull("members_count"))
                    pages.add(groupNodeToPage(groupNode));
            }
            return pages;
        }
    }

    @Override
    public List<JsonNode> getGroupInfoByIds(Collection<Integer> groups) throws Exception {
        log.debug("getGroupInfoByIds groups: {}", groups);
        try (InputStream is = executeByToken(
                "/method/groups.getById",
                "group_ids", groups.stream().map(i -> Integer.toString(i)).collect(Collectors.joining(",")),
                "fields", "members_count,can_message,description,site,status,wiki_page"
        ); BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
            String response = sb.toString();
            log.debug("getGroupsById response {}", response);
            JsonNode groupsNode = OM.readValue(response, new TypeReference<Response<JsonNode>>() {})
                    .getResponse().get("groups");
            List<JsonNode> pages = new LinkedList<>();
            for (JsonNode groupNode : groupsNode) {
                pages.add(groupNode);
            }
            return pages;
        }
    }

    @Override
    public List<Page> getUsersById(Collection<String> users) throws Exception {
        log.debug("getUsersById groups: {}", users);
        try (InputStream is = executeByToken(
                "/method/users.get",
                "user_ids", String.join(",", users),
                "fields", "photo_100, followers_count, can_write_private_message"
        ); BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
            String response = sb.toString();
            log.debug("getUsersById response {}", response);
            JsonNode groupsNode = OM.readValue(response, new TypeReference<Response<JsonNode>>() {})
                    .getResponse();
            List<Page> pages = new LinkedList<>();
            for (JsonNode userNode : groupsNode) {
                if (userNode.hasNonNull("followers_count"))
                    pages.add(userNodeToPage(userNode));
            }
            return pages;
        }
    }

    @Override
    public void send(int id, Message message) throws Exception {
        LinkedList<String> attachments = new LinkedList<>();
        if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
            for (Attachment attachment : message.getAttachments()) {
                String uploaded = files.containsKey(attachment.getFilePatch()) ? files.get(attachment.getFilePatch()) : upload(attachment);
                attachments.add(uploaded);
            }
        }
        if (Objects.nonNull(message.getAttachmentLinkText()) && !message.getAttachmentLinkText().trim().isEmpty()) {
            attachments.addAll(Arrays.asList(message.getAttachmentLinkText().split("\n")));
        }
        try (InputStream is = getSendResult(id, message, attachments); BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String content = sb.toString();
            if (content.contains("\"error\":"))
                throw new RuntimeException(content);
        }
    }

    private InputStream getSendResult(int id, Message message, LinkedList<String> attachments) throws Exception {
        if (attachments.isEmpty()) {
            return executeByToken(
                    "/method/messages.send",
                    "random_id", 0,
                    "dont_parse_links", message.getDontParseLink() ? 1 : 0,
                    "peer_id", id,
                    "message", message.getText()
            );
        } else {
            return executeByToken(
                    "/method/messages.send",
                    "random_id", 0,
                    "peer_id", id,
                    "message", message.getText(),
                    "dont_parse_links", message.getDontParseLink() ? 1 : 0,
                    "attachment", String.join(",", attachments)
            );
        }
    }

    private String upload(Attachment attachment) throws Exception {
        File file = new File(attachment.getFilePatch());
        String mimeType = Files.probeContentType(file.toPath());
        log.info(mimeType);
        String uploadData;
        if (attachment.isDocument() || mimeType == null)
            uploadData = uploadFile(file);
        else if (mimeType.startsWith("image"))
            uploadData = uploadImage(file);
        else if (mimeType.startsWith("video"))
            uploadData = uploadVideo(file);
        else
            uploadData = uploadFile(file);
        files.put(file.getAbsolutePath(), uploadData);
        return uploadData;
    }

    private String uploadVideo(File file) throws Exception {
        JsonNode saveNode;
        try (InputStream is = executeByToken("/method/video.save")) {
            saveNode = OM.readValue(is, new TypeReference<Response<JsonNode>>() {
            }).getResponse();
        }
        HttpURLConnection con = (HttpURLConnection) new URL(saveNode.get("upload_url").asText()).openConnection();
        con.setRequestMethod("POST");
        FileBody fileBody = new FileBody(file);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.STRICT);
        builder.addPart("video_file", fileBody);
        HttpEntity multipartEntity = builder.build();
        con.setRequestProperty("Content-Type", multipartEntity.getContentType().getValue());
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            multipartEntity.writeTo(os);
        }
        try (InputStream is = con.getInputStream()) {
            JsonNode node = OM.readValue(is, JsonNode.class);
            return String.format(
                    "%s%s_%s_%s",
                    "video",
                    node.get("owner_id").asText(),
                    node.get("video_id").asText(),
                    saveNode.get("access_key").asText()
            );
        }
    }

    private String uploadImage(File file) throws Exception {
        JsonNode saveNode;
        try (InputStream is = executeByToken("/method/photos.getMessagesUploadServer")) {
            saveNode = OM.readValue(is, new TypeReference<Response<JsonNode>>() {
            }).getResponse();
        }
        HttpURLConnection con = (HttpURLConnection) new URL(saveNode.get("upload_url").asText()).openConnection();
        con.setRequestMethod("POST");
        FileBody fileBody = new FileBody(file);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.STRICT);
        builder.addPart("photo", fileBody);
        HttpEntity multipartEntity = builder.build();
        con.setRequestProperty("Content-Type", multipartEntity.getContentType().getValue());
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            multipartEntity.writeTo(os);
        }
        JsonNode photoData;
        try (InputStream is = con.getInputStream()) {
            photoData = OM.readValue(is, JsonNode.class);
        }
        try (InputStream is = executeByToken(
                "/method/photos.saveMessagesPhoto",
                "photo", photoData.get("photo").asText(),
                "server", photoData.get("server").asText(),
                "hash", photoData.get("hash").asText()
        )) {
            JsonNode node = OM.readValue(is, JsonNode.class).get("response").get(0);
            log.info(node);
            return String.format(
                    "%s%s_%s_%s",
                    "photo",
                    node.get("owner_id").asText(),
                    node.get("id").asText(),
                    node.get("access_key").asText()
            );
        }
    }

    private String uploadFile(File file) throws Exception {
        JsonNode saveNode;
        try (InputStream is = executeByToken("/method/docs.getMessagesUploadServer", "type", "doc")) {
            saveNode = OM.readValue(is, new TypeReference<Response<JsonNode>>() {
            }).getResponse();
        }
        HttpURLConnection con = (HttpURLConnection) new URL(saveNode.get("upload_url").asText()).openConnection();
        con.setRequestMethod("POST");
        FileBody fileBody = new FileBody(file);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.STRICT);
        builder.addPart("file", fileBody);
        HttpEntity multipartEntity = builder.build();
        con.setRequestProperty("Content-Type", multipartEntity.getContentType().getValue());
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            multipartEntity.writeTo(os);
        }
        String fileData;
        try (InputStream is = con.getInputStream()) {
            fileData = OM.readValue(is, JsonNode.class).get("file").asText();
        }
        try (InputStream is = executeByToken("/method/docs.save", "file", fileData)) {
            JsonNode node = OM.readValue(is, JsonNode.class).get("response");
            return String.format(
                    "%s%s_%s",
                    node.get("type").asText(),
                    node.get(node.get("type").asText()).get("owner_id").asText(),
                    node.get(node.get("type").asText()).get("id").asText()
            );
        }
    }

    @Override
    public Account getAccount() {
        return getUser(token);
    }

    @Override
    public List<EmailItemWrapper<Page>> getHints(String search) throws Exception {
        try (InputStream is = executeByToken(
                "/method/search.getHints",
                "q", search,
                "limit", 200,
                "fields", "members_count, photo_100, can_message, can_write_private_message, followers_count"
        )) {
            return OM.readValue(is, new TypeReference<Response<ItemsResult<JsonNode>>>() {
                    }).getResponse()
                    .getItems()
                    .stream().map(node -> {
                        String type = node.get("type").asText();
                        switch (type) {
                            case "group": {
                                return groupNodeToPage(node.get("group"));
                            }
                            case "profile": {
                                return userNodeToPage(node.get("profile"));
                            }
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .map(page -> new EmailItemWrapper<>(page, search))
                    .collect(Collectors.toList());
        }
    }

    public static Account getUser(String token) {
        try (InputStream is = execute("/method/account.getProfileInfo", token)) {
            JsonNode node = OM.readValue(is, new TypeReference<Response<JsonNode>>() {
            }).getResponse();
            Account account = new Account();
            account.setId(node.get("id").asInt());
            account.setName(node.get("first_name").asText() + ' ' + node.get("last_name").asText());
            account.setToken(token);
            account.setIcon(node.get("photo_200").asText());
            return account;
        } catch (Exception e) {
            log.warn("getUser exception", e);
        }
        return null;
    }

    private InputStream executeByToken(String path, Object... params) throws Exception {
        return execute(path, token, params);
    }

    private static InputStream execute(String path, String token, Object... params) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(BASE_PATH + path).openConnection();
        con.setRequestMethod("POST");
        con.addRequestProperty("accept", "application/json;charset=UTF-8");
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            os.write(("access_token=" + token + '&').getBytes(StandardCharsets.UTF_8));
            os.write(("v=" + VkMessenger.VERSION + '&').getBytes(StandardCharsets.UTF_8));
            if (params.length > 0) {
                for (int i = 0; i < params.length; i += 2) {
                    os.write((params[i].toString() + '=' + params[i + 1].toString() + '&').getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        int status = con.getResponseCode();
        if (status / 100 != 2)
            throw new RuntimeException("status error " + status);
        return con.getInputStream();
    }
}
