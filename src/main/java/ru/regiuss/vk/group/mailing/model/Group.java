package ru.regiuss.vk.group.mailing.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {
    private int id;
    private String name;
    @JsonProperty("members_count")
    private int subscribers;
}
