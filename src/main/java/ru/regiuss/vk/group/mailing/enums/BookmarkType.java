package ru.regiuss.vk.group.mailing.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BookmarkType {
    ALL("Все"),
    USERS("Только пользователи"),
    GROUPS("Только сообщества");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
