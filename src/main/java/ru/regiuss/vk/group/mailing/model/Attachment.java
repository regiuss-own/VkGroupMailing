package ru.regiuss.vk.group.mailing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
    private File file;
    private boolean document;
}
