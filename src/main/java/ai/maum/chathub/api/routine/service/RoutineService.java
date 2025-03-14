package ai.maum.chathub.api.routine.service;

import ai.maum.chathub.api.routine.entity.RoutineDetailEntity;
import ai.maum.chathub.api.routine.entity.RoutineInfoEntity;
import ai.maum.chathub.api.routine.repository.RoutineDetailRepository;
import ai.maum.chathub.api.routine.repository.RoutineInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoutineService {
    private final RoutineInfoRepository routineInfoRepository;
    private final RoutineDetailRepository routineDetailRepository;

    public RoutineInfoEntity getRoutineInfo(String taskCd) {
        return routineInfoRepository.findRoutineInfoEntityByTaskCd(taskCd);
    }

    public List<RoutineDetailEntity> getRoutineDetails(String taskCd) {
        return routineDetailRepository.findRoutineDetailEntitiesByTaskCdOrderBySeq(taskCd);
    }
}