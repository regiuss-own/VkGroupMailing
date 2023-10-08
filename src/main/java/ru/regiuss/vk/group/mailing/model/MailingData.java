package ru.regiuss.vk.group.mailing.model;

import lombok.Data;
import ru.regiuss.vk.group.mailing.messenger.Messenger;

import java.util.List;

@Data
public class MailingData {
    private String search;
    private int minSubscribers;
    private int messageDelay;
    private int groupDelay;
    private List<Message> messages;
}
