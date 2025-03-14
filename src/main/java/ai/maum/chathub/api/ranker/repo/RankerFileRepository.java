package ai.maum.chathub.api.ranker.repo;

import ai.maum.chathub.api.ranker.entity.RankerFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankerFileRepository extends JpaRepository<RankerFileEntity, Long> {
    List<RankerFileEntity> findByRankerHistoryEntity_Id(Long rankerId);
//    List<RankerFileEntity> findRankerFileEntityByRankerId(Long Id);
}
