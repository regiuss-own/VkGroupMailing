package ru.regiuss.vk.group.mailing.model;

import lombok.Data;

import java.util.List;

@Data
public class MailingData {
    private int messageDelay;
    private int groupDelay;
    private List<Message> messages;
}
