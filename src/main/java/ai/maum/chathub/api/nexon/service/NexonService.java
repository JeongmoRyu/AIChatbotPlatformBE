package ai.maum.chathub.api.nexon.service;

import ai.maum.chathub.api.admin.service.TelegramService;
import ai.maum.chathub.api.chat.dto.ChatMessage;
import ai.maum.chathub.api.chat.service.ProtoChatService;
import ai.maum.chathub.api.chatroom.entity.ChatroomEntity;
import ai.maum.chathub.api.chatroom.service.ChatroomService;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.service.MemberService;
import ai.maum.chathub.meta.ResponseMeta;
import ai.maum.chathub.mybatis.vo.ChatroomVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class NexonService {

    @Value("#{'${service.nexon.chatbot-list}'.split(',')}")
    private List<String> chatbotIdList;

    @Value("${service.nexon.user-id}")
    private String userId;

    private final ProtoChatService protoChatService;
    private final MemberService memberService;
    private final ChatroomService chatroomService;
    private final TelegramService telegramService;

    public BaseResponse<String> nexonChat(String message, Long chatbotId) {

        Long startTime = System.currentTimeMillis();

        try {
            telegramService.sendMessage("-1002190830666", chatbotId + ":" + message);
            log.error("telegram send");
        } catch (Exception e) {
            log.error("telegram send error");
        }

        String answer = new  String();
        //nexon용 사용자.
        MemberDetail member = memberService.findMemberByUserId("nexon");
        log.debug("member: {} {} {}", member.getUserKey(), member.getUsername(), member.getName());

        // nexon이 사용 권한이 있는 챗봇인지 확인.
        int idx = chatbotIdList.indexOf(String.valueOf(chatbotId));

        for(String item:chatbotIdList) {
            log.debug("chatbotId: {} ", item);
        }

        log.debug("idx: {}", idx);

        //idx가 0 보다 작으면 권한 없음.
        if(idx < 0) {
            return BaseResponse.failure(answer, ResponseMeta.NOT_ACCESSIBLE_CHATBOT);
        }

        ChatMessage chatMessage = new ChatMessage("user", message);

        //seq는 timestamp 기반으로 생성해준다.
        Long seq = System.currentTimeMillis();
        chatMessage.setSeq(seq);

        log.info("create message sequence:" + seq);

        List<ChatMessage> messages = new ArrayList<ChatMessage>();
        messages.add(chatMessage);

        //nexon 테스트용 사용자 정보 기반 - roomId 생성
        MemberDetail user = memberService.findMemberByUserId(userId);
        log.debug("user: {} {} {}", user.getUserKey(), user.getUsername(), user.getName());

        if(user == null) {
            return BaseResponse.failure(answer, ResponseMeta.NOTEXIST_USERID);
        }

        String chatroomRegUserId = "NEXON_" + member.getUsername(); //외부채널용 chatroom은 채널Type + 사용자ID로 정의.
        //chatroom이 없으면 생성해주는 코드
//        ChatroomVO chatroomVO = chatroomService.getChatroomByRegUserID(chatroomRegUserId);
        ChatroomVO chatroomVO = chatroomService.getChatroomByRegUserIDAndChatbotId(chatroomRegUserId, chatbotId);
        Long chatroomId = chatroomVO == null ? null : chatroomVO.getId();
        String memberId = String.valueOf(user.getUserKey());

        if (chatroomVO == null || chatroomId == null) {  //챗룸은 사용자당 하나씩만 만들어짐. 챗룸이 없으면 신규 생성.
            ChatroomEntity chatroom = new ChatroomEntity();
            chatroom.setTitle("넥슨POC:" + member.getUsername() + ":" + member.getName());
            chatroom.setChatbotId(chatbotId);
            chatroom.setRegUserId(chatroomRegUserId);
            chatroom = chatroomService.setChatroom(chatroom);
            chatroomId = chatroom.getId();
            if (chatroom == null) {
                return BaseResponse.failure(answer, ResponseMeta.NO_CHATROOM);
            }
        }

        try {
            Flux<String> fluxString = protoChatService.startChat(chatbotId, chatroomId, null, messages, memberId, true, user, false);
            answer = fluxString.collectList().map(list -> String.join("", list)).block();
            //kakaoResponse.setData(new KakaoResponse.Data(result));
//            kakaoResponse.setTextResponse(result);
            log.info("result:" + answer);
            answer = answer.replaceAll("\\|\uD83E\uDD16Pong!\\|", "");
        } catch (Exception e) {
            return BaseResponse.failure(answer, ResponseMeta.CHAT_ERROR);
//            throw new RuntimeException(e);
        }
//        result = fluxString.collectList().map(list -> String.join("", list)).block();
//        //kakaoResponse.setData(new KakaoResponse.Data(result));
////            kakaoResponse.setTextResponse(result);
//        log.info("result:" + result);

        Long endTime = System.currentTimeMillis();

        try {
            double elapsedTimeInSeconds = (endTime - startTime) / 1000.0;
            telegramService.sendMessage("-1002190830666", elapsedTimeInSeconds + "초:" + answer);
            log.error("telegram send");
        } catch (Exception e) {
            log.error("telegram send error");
        }

        return BaseResponse.success(answer);
    }
}
