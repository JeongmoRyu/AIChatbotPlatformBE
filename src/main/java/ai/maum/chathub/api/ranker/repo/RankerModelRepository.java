package ai.maum.chathub.api.ranker.repo;

import ai.maum.chathub.api.ranker.entity.RankerModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RankerModelRepository extends JpaRepository<RankerModelEntity, Long> {



}
