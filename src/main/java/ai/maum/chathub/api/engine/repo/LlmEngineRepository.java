package ai.maum.chathub.api.engine.repo;

import ai.maum.chathub.api.engine.entity.LlmEngineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LlmEngineRepository extends JpaRepository<LlmEngineEntity, Long> {
//    List<LlmEngineEntity> findLlmEngineEntitiesByRetrieveEngineIdOrderBySeqAsc(Long retrieveEngindId);
    List<LlmEngineEntity> findLlmEngineEntitiesByVendorOrderBySeqAsc(String vendor);
//    List<LlmEngineEntity> findLlmEngineEntitiesByOOrderBySeqAsc();
    LlmEngineEntity findLlmEngineEntityById(Long id);

    @Query("SELECT DISTINCT e.vendor FROM LlmEngineEntity e")
    List<String> findDistinctVendors();
//    @Query(value = "SELECT DISTINCT(v.vendor) FROM Vendor v", nativeQuery = true)
//    List<String> findDistinctVendors();

}
