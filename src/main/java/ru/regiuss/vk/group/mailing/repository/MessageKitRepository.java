package ru.regiuss.vk.group.mailing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.regiuss.vk.group.mailing.model.Message;
import ru.regiuss.vk.group.mailing.model.MessageKit;

import java.util.List;

@Repository
public interface MessageKitRepository extends JpaRepository<MessageKit, Integer> {
    @Query("select k.messages from MessageKit k where k.id = :kitId")
    List<Message> findAllMessagesByKit(@Param("kitId") Integer kitId);
}
