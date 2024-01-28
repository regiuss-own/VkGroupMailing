package space.regiuss.vk.mailing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import space.regiuss.vk.mailing.model.PageBlacklist;
import space.regiuss.vk.mailing.model.PageId;

import java.util.List;
import java.util.Set;

public interface PageBlacklistRepository extends JpaRepository<PageBlacklist, PageId> {

    @Query("select bl.id from PageBlacklist bl where bl.id in (:ids)")
    Set<PageId> findAllByIdIn(List<PageId> ids);

}
