package space.regiuss.vk.mailing.model;

import lombok.*;
import space.regiuss.vk.mailing.enums.PageMode;
import space.regiuss.vk.mailing.messenger.Messenger;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class ByEmailData {
    private final Messenger messenger;
    private final List<String> listMail;
    private final PageMode mode;
    private final boolean checkDescription;
}
