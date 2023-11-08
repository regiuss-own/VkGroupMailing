package space.regiuss.vk.mailing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import space.regiuss.vk.mailing.model.Message;
import space.regiuss.vk.mailing.model.MessageKit;

import java.util.List;

@Repository
public interface MessageKitRepository extends JpaRepository<MessageKit, Integer> {
    @Query("select k.messages from MessageKit k where k.id = :kitId")
    List<Message> findAllMessagesByKit(@Param("kitId") Integer kitId);
}
