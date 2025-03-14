package ai.maum.chathub.api.routine.repository;

import ai.maum.chathub.api.routine.entity.RoutineScheduleVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface RoutineScheduleMapper {
    public List<RoutineScheduleVO> getSendList();
    public int setSendFlags(Map<String,Object> param);
    public int setSendFlag(Map<String,Object> param);
    public RoutineScheduleVO getSendTargetById(Long id);
    public int setSendFlagBeforeExecute(RoutineScheduleVO routineScheduleVO);
}
