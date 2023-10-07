package ru.regiuss.vk.group.mailing.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private long id;
    @JsonProperty("first_name")
    private String fistName;
    @JsonProperty("last_name")
    private String lastName;
}
