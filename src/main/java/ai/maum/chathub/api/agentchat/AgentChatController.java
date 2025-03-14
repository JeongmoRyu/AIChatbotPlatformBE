package ai.maum.chathub.api.agentchat;


import ai.maum.chathub.api.agentchat.dto.ChatMessage;
import ai.maum.chathub.api.agentchat.service.AgentChatService;
import ai.maum.chathub.api.agentchat.service.AgentProtoChatService;
import ai.maum.chathub.api.chatbot.service.ChatbotService;
import ai.maum.chathub.api.chatbotInfo.service.ChatbotInfoService;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.util.LogUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Agent 채팅", description = "Agent 채팅 및 테스트 로그 탭 관련 API")
public class AgentChatController {
    private final ChatbotService chatbotService;
    private final ChatbotInfoService chatbotInfoService;
    private final AgentChatService agentChatService;
    private final AgentProtoChatService agentProtoChatService;

    @Operation(summary = "Agent 채팅", description = "Agent 채팅-질의")
    @PostMapping({"/agentchat/{chatbot_id}/{chatroom_id}", "/agentchat/{chatbot_id}"})
    public Flux<String> sendChat(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "chatbot_id", required = false) @Parameter(name = "챗봇ID", required = true) Long chatbotId,
            @PathVariable(name = "chatroom_id", required = false) @Parameter(name = "채팅룸ID(채팅히스토리용)", required = false) Long chatroomId,
            @RequestParam(name = "room_id", required = false) @Parameter(name = "룸ID(웹소켓통신용)", required = false) String roomId,
            @RequestParam(name = "grpctest", required = false) @Parameter(name = "grpc 테스트용", required = false) Boolean isGrpcTest,
            @RequestBody @Parameter(name = "채팅메시지", required = true) List<ChatMessage> messages
    ) throws Exception {

        if (isGrpcTest == null)
            isGrpcTest = false;

        if (messages.size() < 1) {
            return Flux.just("");
        }

        if (chatbotId == null || chatbotId < 1) {
            return Flux.just("오류:챗봇ID오류:" + chatbotId);
        }

        if (chatroomId == null || chatroomId < 1) {
            return Flux.just("오류:채팅룸ID오류" + chatroomId);
        }

        String userId = user.getUsername();
        LogUtil.info("userId:" + userId);
        String userKey = String.valueOf(user.getUserKey());


        return agentProtoChatService.startChat(chatbotId, chatroomId, roomId, messages, userKey, false, user);

    }
}