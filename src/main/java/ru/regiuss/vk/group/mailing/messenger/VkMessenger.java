package ru.regiuss.vk.group.mailing.messenger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.Group;
import lombok.extern.log4j.Log4j2;
import ru.regiuss.vk.group.mailing.model.Message;
import ru.regiuss.vk.group.mailing.model.Response;
import ru.regiuss.vk.group.mailing.model.User;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
    public List<Group> search(int page, String search) {
        return null;
    }

    @Override
    public void send(int id, Message message) {

    }

    @Override
    public User getUser() {
        return getUser(token);
    }

    public static User getUser(String token) {
        try (InputStream is = execute(
                "/method/account.getProfileInfo", "POST",
                "access_token", token,
                "v", VERSION
        )) {
            return OM.readValue(is, new TypeReference<Response<User>>() {}).getResponse();
        } catch (Exception e) {
            log.warn("getUser exception", e);
        }
        return null;
    }

    private static InputStream execute(String path, String method, Object... params) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(BASE_PATH + path).openConnection();
        con.setRequestMethod(method);
        if (params.length > 0) {
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
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
