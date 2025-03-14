package ai.maum.chathub.api.engine.repo;

import ai.maum.chathub.api.engine.entity.EngineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface EngineRepository extends JpaRepository<EngineEntity, Long> {
//    @Query("SELECT e FROM EngineEntity e WHERE e.type = :type AND e.useYn='Y' ORDER BY e.type ASC, e.seq ASC")
//    List<EngineEntity> findByTypeOrderByTypeAscSeqAsc(@Param("type") String type);
//
//
//    @Query("SELECT e FROM EngineEntity e WHERE e.useYn='Y' ORDER BY e.type ASC, e.vendor ASC, e.seq ASC")
//    List<EngineEntity> findAllOrderByTypeAscVendorAscSeqAsc();
//
//    @Query("SELECT e FROM EngineEntity e WHERE e.type = :type AND e.vendor = :vendor AND e.useYn='Y' ORDER BY e.seq ASC")
//    List<EngineEntity> findByTypeAndVendorOrderBySeqAsc(@Param("type") String type, @Param("vendor") String vendor);

    // 1. 특정 type만 조회
    @Query(value = "SELECT e.* FROM engine e " +
            "JOIN engine_organization eo ON e.id = eo.engine_id " +
            "WHERE e.type = :type " +
            "AND e.use_yn = 'Y' " +
            "AND eo.organization_id = :organizationId " +
            "ORDER BY e.type ASC, e.seq ASC",
            nativeQuery = true)
    List<EngineEntity> findByTypeOrderByTypeAscSeqAsc(
            @Param("type") String type,
            @Param("organizationId") Long organizationId);

    // 2. 전체 엔진 조회
    @Query(value = "SELECT e.* FROM engine e " +
            "JOIN engine_organization eo ON e.id = eo.engine_id " +
            "WHERE e.use_yn = 'Y' " +
            "AND eo.organization_id = :organizationId " +
            "ORDER BY e.type ASC, e.vendor ASC, e.seq ASC",
            nativeQuery = true)
    List<EngineEntity> findAllOrderByTypeAscVendorAscSeqAsc(
            @Param("organizationId") Long organizationId);

    // 3. 특정 type과 vendor로 조회
    @Query(value = "SELECT e.* FROM engine e " +
            "JOIN engine_organization eo ON e.id = eo.engine_id " +
            "WHERE e.type = :type " +
            "AND e.vendor = :vendor " +
            "AND e.use_yn = 'Y' " +
            "AND eo.organization_id = :organizationId " +
            "ORDER BY e.seq ASC",
            nativeQuery = true)
    List<EngineEntity> findByTypeAndVendorOrderBySeqAsc(
            @Param("type") String type,
            @Param("vendor") String vendor,
            @Param("organizationId") Long organizationId);

}
