package ru.regiuss.vk.group.mailing.model;

import lombok.Data;

@Data
public class Group {
    private int id;
    private String name;
    private int subscribers;
}
