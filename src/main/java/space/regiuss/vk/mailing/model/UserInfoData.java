package space.regiuss.vk.mailing.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class UserInfoData {
    private int userId;
    private String json;
    private JsonNode career;
    private JsonNode occupation;
}
