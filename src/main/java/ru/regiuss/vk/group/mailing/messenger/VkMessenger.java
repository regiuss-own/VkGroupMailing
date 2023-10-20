package ru.regiuss.vk.group.mailing.messenger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import ru.regiuss.vk.group.mailing.model.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class VkMessenger implements Messenger {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final String VERSION = "5.154";
    private static final String BASE_PATH = "https://api.vk.com";
    private final Map<String, String> files;
    private final String token;

    public VkMessenger(String token) {
        this.token = token;
        this.files = new HashMap<>(20);
    }

    @Override
    public List<Group> search(int page, String search) throws Exception {
        try (InputStream is = executeByToken(
                "/method/groups.search", "POST",
                "q", search,
                "sort", 6,
                "offset", 10 * (page-1),
                "count", 10,
                "fields", "members_count"
        )) {
            return OM.readValue(is, new TypeReference<Response<ItemsResult<Group>>>(){}).getResponse().getItems();
        }
    }

    @Override
    public List<Fave> getFaves(int page) throws Exception {
        try (InputStream is = executeByToken(
                "/method/fave.getPages", "POST",
                "offset", 10 * (page-1),
                "count", 10
        )) {
            return OM.readValue(is, new TypeReference<Response<ItemsResult<JsonNode>>>(){}).getResponse().getItems()
                    .stream().map(node -> {
                        String type = node.get("type").asText();
                        JsonNode n = node.get(type);
                        Fave fave = new Fave();
                        fave.setType(type);
                        fave.setId(n.get("id").asInt());
                        if (type.equals("user"))
                            fave.setName(n.get("first_name").asText() + " " + n.get("last_name").asText());
                        else
                            fave.setName(n.get("name").asText());
                        return fave;
                    }).collect(Collectors.toList());
        }
    }

    @Override
    public void send(int id, Message message) throws Exception {
        LinkedList<String> attachments = new LinkedList<>();
        if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
            for (Attachment attachment : message.getAttachments()) {
                String uploaded = files.containsKey(attachment.getFile().getAbsolutePath()) ? files.get(attachment.getFile().getAbsolutePath()) : upload(attachment);
                attachments.add(uploaded);
            }
        }
        if (attachments.isEmpty()) {
            executeByToken(
                    "/method/messages.send", "POST",
                    "random_id", 0,
                    "peer_id", id,
                    "message", message.getText()
            ).close();
        } else {
            executeByToken(
                    "/method/messages.send", "POST",
                    "random_id", 0,
                    "peer_id", id,
                    "message", message.getText(),
                    "attachment", String.join(",", attachments)
            ).close();
        }
    }

    private String upload(Attachment attachment) throws Exception {
        String mimeType = Files.probeContentType(attachment.getFile().toPath());
        log.info(mimeType);
        String uploadData;
        if (attachment.isDocument() || mimeType == null)
            uploadData = uploadFile(attachment.getFile());
        else if(mimeType.startsWith("image"))
            uploadData = uploadImage(attachment.getFile());
        else if(mimeType.startsWith("video"))
            uploadData = uploadVideo(attachment.getFile());
        else
            uploadData = uploadFile(attachment.getFile());
        files.put(attachment.getFile().getAbsolutePath(), uploadData);
        return uploadData;
    }

    private String uploadVideo(File file) throws Exception {
        JsonNode saveNode;
        try (InputStream is = executeByToken("/method/video.save", "POST")) {
            saveNode = OM.readValue(is, new TypeReference<Response<JsonNode>>() {}).getResponse();
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
        try (InputStream is = executeByToken("/method/photos.getMessagesUploadServer", "POST")) {
            saveNode = OM.readValue(is, new TypeReference<Response<JsonNode>>() {}).getResponse();
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
                "/method/photos.saveMessagesPhoto", "POST",
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
        try (InputStream is = executeByToken("/method/docs.getMessagesUploadServer", "POST", "type", "doc")) {
            saveNode = OM.readValue(is, new TypeReference<Response<JsonNode>>() {}).getResponse();
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
        try (InputStream is = executeByToken("/method/docs.save", "POST", "file", fileData)) {
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
    public User getUser() {
        return getUser(token);
    }

    public static User getUser(String token) {
        try (InputStream is = execute("/method/account.getProfileInfo", "POST", token)) {
            return OM.readValue(is, new TypeReference<Response<User>>() {}).getResponse();
        } catch (Exception e) {
            log.warn("getUser exception", e);
        }
        return null;
    }

    private InputStream executeByToken(String path, String method, Object... params) throws Exception {
        return execute(path, method, token, params);
    }

    private static InputStream execute(String path, String method, String token, Object... params) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(BASE_PATH + path).openConnection();
        con.setRequestMethod(method);
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            os.write(("access_token=" + token + '&').getBytes(StandardCharsets.UTF_8));
            os.write(("v=" + VkMessenger.VERSION + '&').getBytes(StandardCharsets.UTF_8));
            if (params.length > 0) {
                for (int i = 0; i < params.length; i+=2) {
                    os.write((params[i].toString() + '=' + params[i+1].toString() + '&').getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        int status = con.getResponseCode();
        if (status / 100 != 2)
            throw new RuntimeException("status error " + status);
        return con.getInputStream();
    }
}
