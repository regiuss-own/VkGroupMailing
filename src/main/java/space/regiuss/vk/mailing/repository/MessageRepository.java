package space.regiuss.vk.mailing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import space.regiuss.vk.mailing.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
}
