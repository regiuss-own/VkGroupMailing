package space.regiuss.vk.mailing.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DescriptionMode {

    NONE("Не проверять"),
    ANY("Любой из списка"),
    ALL("Все");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

}
