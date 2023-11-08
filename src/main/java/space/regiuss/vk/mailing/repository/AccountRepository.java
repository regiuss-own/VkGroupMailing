package space.regiuss.vk.mailing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import space.regiuss.vk.mailing.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
}
