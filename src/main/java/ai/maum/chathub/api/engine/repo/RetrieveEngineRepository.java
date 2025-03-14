package ai.maum.chathub.api.engine.repo;

import ai.maum.chathub.api.engine.entity.RetrieveEngineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetrieveEngineRepository extends JpaRepository<RetrieveEngineEntity, Long> {
    List<RetrieveEngineEntity> findByVendorOrderBySeqAsc(String vendor);
    RetrieveEngineEntity findRetrieveEngineEntityById(Long id);

}
