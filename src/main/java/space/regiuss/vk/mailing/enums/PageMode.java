package space.regiuss.vk.mailing.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PageMode {
    ALL("Все"),
    USERS("Только пользователи"),
    GROUPS("Только сообщества");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
