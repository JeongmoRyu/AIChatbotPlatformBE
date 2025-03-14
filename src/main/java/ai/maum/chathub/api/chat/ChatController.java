package ai.maum.chathub.api.chat;

import ai.maum.chathub.api.chat.dto.ChatMessage;
import ai.maum.chathub.api.chat.service.ChatService;
import ai.maum.chathub.api.chat.service.ProtoChatService;
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
@Tag(name = "채팅", description = "채팅 및 테스트 로그 탭 관련 API")
public class ChatController {
    private final ChatService chatService;
    private final ChatbotService chatbotService;
    private final ProtoChatService protoChatService;
    private final ChatbotInfoService chatbotInfoService;

    @Operation(summary = "채팅", description = "채팅-질의")
    @PostMapping({"/chat/{chatbot_id}/{chatroom_id}", "/chat/{chatbot_id}"})
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

//        LogUtil.info("user_id: " + user.getUsername());
//        LogUtil.info("chatbot_id: " + chatbotId);
//        LogUtil.info("chatroomId: " + chatroomId);
//        LogUtil.info("room_id: " + roomId);
//        LogUtil.info("messages: " + ObjectMapperUtil.writeValueAsString(messages));
//        LogUtil.debug("isGrpcTest:" + isGrpcTest);

        if (messages.size() < 1) {
            //질문이 없는것 이므로 그냥 리턴.
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

        //임시코드 - 계정 관련 내용 정리 되면 없애야 함.
//        String userKey = "100000000" + String.valueOf(chatbotId);;
        String userKey = String.valueOf(user.getUserKey());

//        Flux<String> fluxString = protoChatService.startChat(chatbotId, chatroomId, roomId, messages, userKey, false, user);
//
//        fluxString.subscribe(
//                System.out::println,
//                error -> System.err.println("Error: " + error.getMessage()),
//                () -> System.out.println("Complete")
//        );
//
//        return fluxString;

        return protoChatService.startChat(chatbotId, chatroomId, roomId, messages, userKey, false, user);

//        ChatbotVO chatbot = chatbotService.getChatbotByIdWithMapper(chatbotId);
//        List<HashMap<String,Object>> chatbotInfo = chatbotInfoService.getChatbotInfoMap(chatbotId);
//        return chatService.startChat(chatbot, chatroomId, roomId,  messages);

        // Type = AMR 일때는 모든 RAG,LLM 관련 처리를 gRPC로 함. (신규로직)
        // Type = AMR이 아닐때는 RAG, LLM 관련 처리를 JAVA에서 직접 수행함. (기존 로직)
//        if("AMR".equals(chatbot.getChatbotTypeCd()))
//            if(isGrpcTest || chatbot.getId() >= 4 )
//                return protoChatService.startChat(chatbotInfo, chatroomId, roomId,  messages, null);
//            else
//                return chatService.startChat(chatbot, chatroomId, roomId,  messages);
//        else
//            return chatService.startChat(chatbot, chatroomId, roomId,  messages);
    }
}
