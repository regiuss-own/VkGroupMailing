package space.regiuss.vk.mailing.model;

import lombok.Data;

import java.util.List;

@Data
public class MailingData {
    private int messageDelay;
    private int dialogDelay;
    private List<Message> messages;
    List<ProgressItemWrapper<Page>> items;
}
