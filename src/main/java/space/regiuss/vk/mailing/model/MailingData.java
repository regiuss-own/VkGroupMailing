package space.regiuss.vk.mailing.model;

import lombok.Data;
import space.regiuss.vk.mailing.wrapper.ProgressItemWrapper;

import java.util.List;

@Data
public class MailingData {
    private int messageDelay;
    private int dialogDelay;
    private int maxErrorCount;
    private int onErrorDelay;
    private List<Message> messages;
    List<ProgressItemWrapper<Page>> items;
}
