package space.regiuss.vk.mailing.model;

import lombok.Data;
import space.regiuss.vk.mailing.repository.PageBlacklistRepository;

@Data
public class ProfileTaskData {
    private String[] groups;
    private int minSubscribersCount;
    private int maxSubscribersCount;
    private boolean onlyCanMessage;
    private PageBlacklistRepository pageBlacklistRepository;
}
