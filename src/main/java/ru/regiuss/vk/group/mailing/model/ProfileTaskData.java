package ru.regiuss.vk.group.mailing.model;

import lombok.Data;

@Data
public class ProfileTaskData {
    private String group;
    private int minSubscribersCount;
    private int maxSubscribersCount;
}
