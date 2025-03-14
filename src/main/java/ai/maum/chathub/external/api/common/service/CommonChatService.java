package ai.maum.chathub.external.api.common.service;

import ai.maum.chathub.api.chat.dto.ChatMessage;
import ai.maum.chathub.api.chat.service.ProtoChatService;
import ai.maum.chathub.api.chatbot.service.ChatbotService;
import ai.maum.chathub.api.chatroom.entity.ChatroomEntity;
import ai.maum.chathub.api.chatroom.service.ChatroomService;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.service.MemberService;
import ai.maum.chathub.meta.ResponseMeta;
import ai.maum.chathub.mybatis.vo.ChatroomVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommonChatService {

    private final MemberService memberService;
    private final ChatroomService chatroomService;
    private final ProtoChatService protoChatService;

    public Boolean checkAuthorize(String vendorId, String userId, Long chatbodId) {

        return true;
    }

    public BaseResponse<String> chat(String message, Long chatbotId, String userId) {
        Long startTime = System.currentTimeMillis();
        String answer = new  String();

        ChatMessage chatMessage = new ChatMessage("user", message);

        //seq는 timestamp 기반으로 생성해준다.
        Long seq = System.currentTimeMillis();
        chatMessage.setSeq(seq);

        log.info("create message sequence:" + seq);

        List<ChatMessage> messages = new ArrayList<ChatMessage>();
        messages.add(chatMessage);

        MemberDetail user = memberService.findMemberByUserId(userId);
        log.debug("user: {} {} {}", user.getUserKey(), user.getUsername(), user.getName());

        ChatroomVO chatroomVO = chatroomService.getChatroomByRegUserIDAndChatbotId(userId, chatbotId);
        Long chatroomId = chatroomVO == null ? null : chatroomVO.getId();
        String memberId = String.valueOf(user.getUserKey());

        if (chatroomVO == null || chatroomId == null) {  //챗룸은 사용자당 하나씩만 만들어짐. 챗룸이 없으면 신규 생성.
            ChatroomEntity chatroom = new ChatroomEntity();
            chatroom.setTitle("API:" + user.getUsername() + ":" + user.getName());
            chatroom.setChatbotId(chatbotId);
            chatroom.setRegUserId(userId);
            chatroom = chatroomService.setChatroom(chatroom);
            chatroomId = chatroom.getId();
            if (chatroom == null) {
                return BaseResponse.failure(answer, ResponseMeta.NO_CHATROOM);
            }
        }

        try {
            Flux<String> fluxString = protoChatService.startChat(chatbotId, chatroomId, null, messages, memberId, true, user, false);
            answer = fluxString.collectList().map(list -> String.join("", list)).block();
            log.info("result:" + answer);
            answer = answer.replaceAll("\\|\uD83E\uDD16Pong!\\|", "");
        } catch (Exception e) {
            return BaseResponse.failure(answer, ResponseMeta.CHAT_ERROR);
        }

        Long endTime = System.currentTimeMillis();

        log.debug("Chat time : {} ms", endTime - startTime);

        return BaseResponse.success(answer);
    }

}
