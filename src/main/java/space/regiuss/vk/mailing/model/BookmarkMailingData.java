package space.regiuss.vk.mailing.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import space.regiuss.vk.mailing.enums.PageMode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BookmarkMailingData extends MailingData {
    private PageMode type;
}
