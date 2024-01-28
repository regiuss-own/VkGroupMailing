package space.regiuss.vk.mailing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import space.regiuss.vk.mailing.model.Page;
import space.regiuss.vk.mailing.model.PageId;

@Repository
public interface PageRepository extends JpaRepository<Page, PageId> {

}
