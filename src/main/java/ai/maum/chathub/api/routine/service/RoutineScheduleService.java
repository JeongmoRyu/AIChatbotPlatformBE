package ai.maum.chathub.api.routine.service;

import ai.maum.chathub.api.kakao.service.KakaoService;
import ai.maum.chathub.api.routine.entity.RoutineScheduleVO;
import ai.maum.chathub.api.routine.repository.RoutineScheduleMapper;
import ai.maum.chathub.util.ObjectMapperUtil;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
//@Profile("dev")
public class RoutineScheduleService {

    private final KakaoService kakaoService;
    private final RoutineScheduleMapper routineScheduleMapper;
    private final MemberService memberService;

//    @Scheduled(fixedRate = 1 * 60 * 1000) // 1분마다 실행
    public void executeRoutine() {
        log.info("-------------- ROUTINE START ---------------------");
        List<RoutineScheduleVO> sendList = routineScheduleMapper.getSendList();
        List<Long> idList = new ArrayList<Long>();
        for(RoutineScheduleVO item:sendList) {
            log.info("list:" + ObjectMapperUtil.writeValueAsString(item));
//            idList.add(item.getId());
            int updateCnt = routineScheduleMapper.setSendFlagBeforeExecute(item);
            log.info("updateCnt(beforeexecute):" + updateCnt);
            if(updateCnt == 1) {
                //상태가 변경 되었으면 처리한다.
                String taskCd = item.getTaskCd();
                String phone = item.getReceiveId();
                String name = item.getName();

                log.info("send friend talk:" + taskCd + ":" + phone + ":");

                String result = new String();
                BaseResponse baseResponse = null;

                if(phone == null || phone.isBlank()) {
                    List<MemberDetail> members = memberService.findMemberByName(name);
                    if(members == null || members.size() < 1)
                        result = "사용자없음:" + name + "\n";
                    else if(members.size() > 1)
                        result = "동명이인:" + members.size() + "명:" + name + "\n";
                    else {
                        phone = members.get(0).getReceiveId();
                        if(phone == null || phone.isBlank())
                            result = "폰번호없음:" + name + "\n";
                        else {
                            baseResponse = kakaoService.sendFriendTalk(taskCd, phone);
                            result = name + ":" + phone + ":" + ObjectMapperUtil.writeValueAsString(baseResponse.getData()) + "\n";
                        }
                    }
                } else {
                    baseResponse = kakaoService.sendFriendTalk(taskCd, phone);
                    result = ObjectMapperUtil.writeValueAsString(baseResponse.getData());
                }

                log.info("send friend talk send result:" + taskCd + ":" + phone + ":" + result);
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("id", item.getId());
                if(baseResponse != null && baseResponse.getResult())
                    param.put("sendYn", "Y");
                else
                    param.put("sendYn", "E");
                param.put("result", result);
                updateCnt = routineScheduleMapper.setSendFlag(param);
                log.info("send friend talk send result updatecnt :" + taskCd + ":" + phone + ":" + result + ":" + updateCnt);
            } else {
                log.info("caution!!! state change!!! :" + item.getTaskCd() + ":" + item.getReceiveId());
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("id", item.getId());
                param.put("sendYn", "E");
                param.put("result", "state changed!!!");
                updateCnt = routineScheduleMapper.setSendFlag(param);
            }
        }
    }
}
