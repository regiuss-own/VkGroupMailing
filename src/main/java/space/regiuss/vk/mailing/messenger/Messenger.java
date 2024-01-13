package space.regiuss.vk.mailing.messenger;

import space.regiuss.vk.mailing.model.*;

import java.util.Collection;
import java.util.List;

public interface Messenger {
    List<Page> search(int page, String search, boolean sort) throws Exception;

    List<Page> getFaves(int page) throws Exception;

    ItemsResult<Integer> getGroupMembers(String group, int page) throws Exception;

    List<UserInfoData> getUserInfoByIds(List<Integer> userIds) throws Exception;

    List<Page> getGroupsById(Collection<String> groups) throws Exception;

    List<Page> getUsersById(Collection<String> users) throws Exception;

    void send(int id, Message message) throws Exception;

    Account getAccount();
}
