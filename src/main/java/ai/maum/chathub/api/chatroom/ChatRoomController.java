package ai.maum.chathub.api.chatroom;

import ai.maum.chathub.api.chatroom.service.ChatroomService;
import ai.maum.chathub.util.LogUtil;
import ai.maum.chathub.api.chatroom.entity.ChatroomDetailEntity;
import ai.maum.chathub.api.chatroom.entity.ChatroomEntity;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ai.maum.chathub.api.chatroom.entity.ChatroomDateEntity;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name="채팅룸", description="채팅목록 및 대화내용 관련 API")
public class ChatRoomController {
    private final ChatroomService chatRoomService;

    @Operation(summary = "조회", description = "특정 챗봇의 채팅룸 조회(내 계정으로 생성된 채팅룸만 조회)")
    @ResponseBody
    @GetMapping({"/chatroom/{chatbot_id}"})
    public BaseResponse<List<ChatroomEntity>> getChatrooms (
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "chatbot_id", required = false) @Parameter(description = "챗봇 ID", required = true) Long chatbotId
    ) {
        List<ChatroomEntity> chatroomList = new ArrayList<ChatroomEntity>();
        try {
            chatroomList = chatRoomService.getMyChatroomList(chatbotId, user.getUsername());
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
        return BaseResponse.success(chatroomList);
    }

    @Operation(summary = "채팅내용조회", description = "특정 채팅룸의 채팅 내용 조회")
    @ResponseBody
    @GetMapping({"/chatroom/detail/{chatroom_id}"})
    public BaseResponse<List<ChatroomDetailEntity>> getChatroomDetail (
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "chatroom_id", required = false) @Parameter(description = "채팅룸ID", required = true) Long chatroomId
    ) {
        List<ChatroomDetailEntity> chatroomDetailList = new ArrayList<ChatroomDetailEntity>();
        try {
            chatroomDetailList = chatRoomService.getChatroomDetail(chatroomId);
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
        return BaseResponse.success(chatroomDetailList);
    }

    @Operation(summary = "채팅룸생성", description = "챗봇내에 채팅룸생성-새로운대회시작전 호출 필요")
    @ResponseBody
    @PostMapping({"/chatroom/{chatbot_id}"})
    public BaseResponse<ChatroomEntity> setChatroom (
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "chatbot_id", required = true) @Parameter(description = "챗봇ID", required = true) Long chatbotId,
            @RequestBody @Parameter(name = "챗봇상세", required = true) ChatroomEntity chatroom
    ) {
        chatroom.setRegUserId(user.getUsername());
        //채팅룸 제목이 없을때는 기본 채팅룸 이름을 넣어줌.
        chatroom.setChatbotId(chatbotId);
        return BaseResponse.success(chatRoomService.setChatroom(chatroom));
    }

    @Operation(summary = "피드백 저장", description = "특정 응답의 피드백을 저장챗봇내에 채팅룸생성")
    @ResponseBody
    @PostMapping({"/chatroom/feedback/{chatroom_id}/{seq}"})
    public BaseResponse<Integer> setChatroom(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "chatroom_id", required = true) @Parameter(description = "챗룸ID", required = true) Long chatroomId,
            @PathVariable(name = "seq", required = true) @Parameter(description = "seq", required = true) Long seq,
            @RequestParam(name = "feedback", required=true) @Parameter(name = "피드백내용", required = true) String feedback,
            @RequestParam(name = "user_name", required=false) @Parameter(name = "사용자이름", required = false) String userName
    ) {
        return BaseResponse.success(chatRoomService.setFeedBack(chatroomId, seq, feedback, userName));
    }

    //    new chat history with date
    @Operation(summary = "날짜별 채팅룸 조회", description = "특정 챗봇의 채팅룸을 날짜별로 그룹화하여 조회(내 계정으로 생성된 채팅룸만 조회)")
    @ResponseBody
    @GetMapping("/chatroom/date/{chatbot_id}")
    public BaseResponse<ChatroomDateEntity> getChatroomsByDateGroup(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "chatbot_id") @Parameter(description = "챗봇 ID", required = true) Long chatbotId) {

        return BaseResponse.success(chatRoomService.getChatroomsByDateGroup(chatbotId, user.getUsername()));
    }

}
