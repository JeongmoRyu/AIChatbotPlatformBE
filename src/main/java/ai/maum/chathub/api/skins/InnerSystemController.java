package ai.maum.chathub.api.skins;

import ai.maum.chathub.api.chatroom.entity.ChatroomDetailEntity;
import ai.maum.chathub.api.chatroom.service.ChatroomService;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.service.MemberDetailService;
import ai.maum.chathub.api.kakao.dto.FriendTalkResponse;
import ai.maum.chathub.api.kakao.service.KakaoService;
import ai.maum.chathub.util.LogUtil;
import ai.maum.chathub.util.ObjectMapperUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name="아모레내부시스템용API", description="아모레내부시스템용API")
@RequestMapping("/extapi/inner")
public class InnerSystemController {

    private final ChatroomService chatroomService;
    private final MemberDetailService memberDetailService;
    private final KakaoService kakaoService;

    @Operation(summary = "채팅내용조회", description = "특정 채팅룸의 채팅 내용 조회")
    @ResponseBody
    @GetMapping({"/chatroom/detail/{user_key}"})
    public BaseResponse<List<ChatroomDetailEntity>> getChatroomDetail (
            @PathVariable(name = "user_key", required = false) @Parameter(description = "사용자Key", required = true) String userKey
    ) {

        userKey = "KAKAOCHAT_" + userKey;

        List<ChatroomDetailEntity> chatroomDetailList = new ArrayList<ChatroomDetailEntity>();
        try {
            chatroomDetailList = chatroomService.getChatroomDetailByRegUserId(userKey);
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
        return BaseResponse.success(chatroomDetailList);
    }

    @Operation(summary = "톡(친구톡) 발송", description = "사용자(카카오채팅룸)에게 톡 보내기-선톡기능 활용")
    @ResponseBody
    @PostMapping({"/sendtalk/{user_key}"})
    public BaseResponse sendFriendTalk (
            @PathVariable(name = "user_key", required = false) @Parameter(description = "사용자Key", required = true) String userKey,
            @RequestParam(name="message", required=false) @Parameter(name = "채팅메시지", required = true) String message
    ) {

        try {

            MemberDetail memberDetail = memberDetailService.findMemberByUserKey(userKey);

            if (memberDetail == null) {
                return BaseResponse.failure("없는 사용자 입니다.");
            } else if (memberDetail != null
                    && (memberDetail.getReceiveId() == null || memberDetail.getReceiveId().isBlank())) {
                return BaseResponse.failure("휴대폰 번호가 없습니다.");
            }

            String receiveId = memberDetail.getReceiveId();

            String resString = kakaoService.sendFriendTalkSimpleMessage(receiveId, message);
            FriendTalkResponse friendTalkResponse = ObjectMapperUtil.readValue(resString, FriendTalkResponse.class);
            String rtnMessage = friendTalkResponse.getMessage();
            if("성공".equals(rtnMessage))
                return BaseResponse.success(rtnMessage);
            else
                return BaseResponse.failure(rtnMessage);
        } catch (Exception e) {
            return BaseResponse.failure(e.getMessage());
        }




    }


}
