package space.regiuss.vk.mailing.model;

import lombok.*;
import space.regiuss.vk.mailing.enums.DescriptionMode;
import space.regiuss.vk.mailing.enums.PageMode;
import space.regiuss.vk.mailing.messenger.Messenger;
import space.regiuss.vk.mailing.repository.PageBlacklistRepository;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
public class FastSearchData {
    private final Messenger messenger;
    private final List<String> listMail;
    private final PageMode pageMode;
    private final DescriptionMode descriptionMode;
    private final PageBlacklistRepository pageBlacklistRepository;
    private final int tryCount;
    private final Set<String> descriptionWords;
}
