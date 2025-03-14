package ai.maum.chathub.api.routine.repository;

import ai.maum.chathub.api.routine.entity.RoutineInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutineInfoRepository extends JpaRepository<RoutineInfoEntity, Long> {
    RoutineInfoEntity findRoutineInfoEntityByTaskCd(String taskCd);
}
