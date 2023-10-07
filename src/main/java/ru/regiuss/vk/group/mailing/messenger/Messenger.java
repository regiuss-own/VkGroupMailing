package ru.regiuss.vk.group.mailing.messenger;

import ru.regiuss.vk.group.mailing.model.Group;
import ru.regiuss.vk.group.mailing.model.Message;
import ru.regiuss.vk.group.mailing.model.User;

import java.util.List;

public interface Messenger {
    List<Group> search(int page, String search) throws Exception;

    void send(int id, Message message);

    User getUser();
}
