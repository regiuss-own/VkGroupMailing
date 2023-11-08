package space.regiuss.vk.mailing.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemsResult<T> {
    private long count;
    private List<T> items;
}
