package ai.maum.chathub.api.ranker.repo;

import ai.maum.chathub.api.ranker.entity.RankerQaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankerQaRepository extends JpaRepository<RankerQaEntity, Long> {
    Page<RankerQaEntity> findByRankerHistoryEntity_Id(Long rankerId, Pageable pageable);
}
