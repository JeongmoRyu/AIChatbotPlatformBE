package ai.maum.chathub.api.ranker.repo;

import ai.maum.chathub.api.ranker.entity.RankerModelEnsembleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RankerModelEnsembleRepository extends JpaRepository<RankerModelEnsembleEntity, Long> {

}
