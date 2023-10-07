package ru.regiuss.vk.group.mailing.messenger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import ru.regiuss.vk.group.mailing.model.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class VkMessenger implements Messenger {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final String VERSION = "5.154";
    private static final String BASE_PATH = "https://api.vk.com";
    private final String token;

    public VkMessenger(String token) {
        this.token = token;
    }

    @Override
    public List<Group> search(int page, String search) throws Exception {
        String ids;
        try (InputStream is = executeByToken(
                "/method/groups.search", "POST",
                "q", search,
                "sort", 6,
                "offset", 10 * (page-1),
                "count", 10
        )) {
            ids = OM.readValue(is, new TypeReference<Response<ItemsResult<JsonNode>>>(){}).getResponse()
                    .getItems().stream().map(node -> node.get("id").asText()).collect(Collectors.joining(","));
        }
        if (ids.isEmpty())
            return Collections.emptyList();
        try (InputStream is = executeByToken(
                "/method/groups.getById", "POST",
                "group_ids", ids,
                "fields", "members_count"
        )) {
            return OM.readValue(is, new TypeReference<Response<Map<String, List<Group>>>>(){}).getResponse().get("groups");
        }
    }

    @Override
    public void send(int id, Message message) {

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
