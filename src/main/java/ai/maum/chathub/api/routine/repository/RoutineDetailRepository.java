package ai.maum.chathub.api.routine.repository;

import ai.maum.chathub.api.routine.entity.RoutineDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutineDetailRepository extends JpaRepository<RoutineDetailEntity, Long> {
    List<RoutineDetailEntity> findRoutineDetailEntitiesByTaskCdOrderBySeq(String taskCd);
}
