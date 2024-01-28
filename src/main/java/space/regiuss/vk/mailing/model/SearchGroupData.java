package space.regiuss.vk.mailing.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import space.regiuss.vk.mailing.repository.PageBlacklistRepository;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SearchGroupData extends MailingData {
    private List<String> search;
    private int minSubscribers;
    private int maxSubscribers;
    private boolean sort;
    private boolean onlyCanMessage;
    private PageBlacklistRepository pageBlacklistRepository;
}
