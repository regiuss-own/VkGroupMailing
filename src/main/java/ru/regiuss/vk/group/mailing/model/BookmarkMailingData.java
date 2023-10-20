package ru.regiuss.vk.group.mailing.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.regiuss.vk.group.mailing.enums.BookmarkType;

@EqualsAndHashCode(callSuper = true)
@Data
public class BookmarkMailingData extends MailingData {
    private BookmarkType type;
}
