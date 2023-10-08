package ru.regiuss.vk.group.mailing.model;

import lombok.Data;

import java.io.File;

@Data
public class Attachment {
    private File file;
    private boolean document;
}
