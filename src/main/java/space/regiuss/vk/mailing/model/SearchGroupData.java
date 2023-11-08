package space.regiuss.vk.mailing.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SearchGroupData extends MailingData {
    private String search;
    private int minSubscribers;
    private int maxSubscribers;
    private boolean sort;
    private boolean onlyCanMessage;
}
