package ru.regiuss.vk.group.mailing.model;

import lombok.Data;

@Data
public class Response<T> {
    private T response;
}
