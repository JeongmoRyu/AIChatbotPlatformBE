package ai.maum.chathub.api.routine;

import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.service.MemberService;
import ai.maum.chathub.api.kakao.service.KakaoService;
import ai.maum.chathub.util.ObjectMapperUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name="루틴", description="루틴관리API")
@RequestMapping("/extapi/routine")
public class RoutineController {

    private final KakaoService kakaoService;
    private final MemberService memberService;

    @Operation(summary = "루틴실행", description = "루틴실행")
    @ResponseBody
    @PostMapping("/execute")
    public BaseResponse actionRoutine(
            @RequestBody @Parameter(name = "카카오Param", required = false) Map<String,Object> paramObject
            ) {
        log.info("execute routine!!!");

        String phoneNumber = (String) paramObject.get("phone");
        String taskCd = (String) paramObject.get("task_cd");
        List<String> phoneNumbers = (List<String>) paramObject.get("phones");
        List<String> names = (List<String>) paramObject.get("names");

        log.info("task:" + taskCd);
        log.info("phone:" + phoneNumber);
        log.info("phones:" + phoneNumbers!=null? ObjectMapperUtil.writeValueAsString(phoneNumbers):"");
        log.info("names:" + names!=null? ObjectMapperUtil.writeValueAsString(names):"");

        String token = kakaoService.getAccessToken();

        String rtnString = "";
        BaseResponse baseResponse = null;

        if(phoneNumber != null) {
            baseResponse = kakaoService.sendFriendTalk(taskCd, phoneNumber);
            rtnString = ObjectMapperUtil.writeValueAsString(baseResponse.getData());

        }
//        return kakaoService.sendFriendTalk(taskCd, phoneNumber);
        if(phoneNumbers != null && phoneNumbers.size() > 1) {
            for(String phone : phoneNumbers) {
                baseResponse = kakaoService.sendFriendTalk(taskCd, phone);
                rtnString +=  ObjectMapperUtil.writeValueAsString(baseResponse.getData()) + "\n";
            }
        }

        if(names != null && names.size() > 0) {
            for(String name : names) {
                List<MemberDetail> members = memberService.findMemberByName(name);
                if(members == null || members.size() < 1)
                    rtnString+= "사용자없음:" + name + "\n";
                else if(members.size() > 1)
                    rtnString+= "동명이인:" + members.size() + "명:" + name + "\n";
                else {
                    String phone = members.get(0).getReceiveId();
                    if(phone == null || phone.isBlank())
                        rtnString+= "폰번호없음:" + name + "\n";
                    else {
                        baseResponse = kakaoService.sendFriendTalk(taskCd, phone);
                        rtnString += name + ":" + phone + ":" + ObjectMapperUtil.writeValueAsString(baseResponse.getData()) + "\n";

                    }
                }
            }
        }

        return BaseResponse.success(rtnString);
    }

}
