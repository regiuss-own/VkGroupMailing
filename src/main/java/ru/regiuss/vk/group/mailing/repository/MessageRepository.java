package ru.regiuss.vk.group.mailing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.regiuss.vk.group.mailing.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
}
