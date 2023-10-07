package ru.regiuss.vk.group.mailing.model;

import lombok.Data;

import java.io.File;
import java.util.List;

@Data
public class Message {
    private String text;
    private List<File> files;
}
