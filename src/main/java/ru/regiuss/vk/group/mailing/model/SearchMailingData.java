package ru.regiuss.vk.group.mailing.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SearchMailingData extends MailingData {
    private String search;
    private int minSubscribers;
    private int maxSubscribers;
}
