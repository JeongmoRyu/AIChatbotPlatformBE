package ai.maum.chathub.api.ranker.repo;

import ai.maum.chathub.api.ranker.entity.RankerQaEntity;
import ai.maum.chathub.api.ranker.entity.RankerRankingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RankerRankingRepository extends JpaRepository<RankerRankingEntity, Long> {
    Page<RankerRankingEntity> findByRankerHistoryEntity_Id(Long rankerId, Pageable pageable);
}
