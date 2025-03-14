package ai.maum.chathub.api.kakao.service;

import ai.maum.chathub.api.chat.dto.ChatMessage;
import ai.maum.chathub.api.chat.service.ProtoChatService;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.kakao.dto.KakaoResponse;
import ai.maum.chathub.util.MarkdownUtil;
import ai.maum.chathub.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoAsyncService {

    private final ProtoChatService protoChatService;
    private final KakaoService kakaoService;
    @Async
    public void afterChat(Long chatbotId, Long chatroomId, List<ChatMessage> messages, String memberId, Long startTime,
                          String receiveId, LinkedHashMap userRequest, MemberDetail member
    ) {

        String result = "";
        KakaoResponse kakaoResponse = new KakaoResponse();
        Boolean bSendResponse = false;
        Boolean bFriendTalk = false;
        String callbackUrl = (String) userRequest.get("callbackUrl");

        try {

            log.info("start Chat:" + chatbotId + ":" + chatroomId + ":" + ObjectMapperUtil.writeValueAsString(messages) + ":" + memberId);

            Flux<String> fluxString = protoChatService.startChat(chatbotId, chatroomId, null, messages, memberId, true, member);
            result = fluxString.collectList().map(list -> String.join("", list)).block();
            //kakaoResponse.setData(new KakaoResponse.Data(result));
//            kakaoResponse.setTextResponse(result);
            log.info("result:" + result);
            result = result.replaceAll("\\|\uD83E\uDD16Pong!\\|", "");
//            log.info("result.replace:" + result.replaceAll("\\|\uD83E\uDD16Pong!\\|", ""));

            //결과를 markdown -> plain text로 변환
            result = MarkdownUtil.convertMarkdownToText(result);
            log.info("result.replace:" + result);

            kakaoResponse.setOutputSimpleText(result);

            Long endTime = System.currentTimeMillis();
            Long diffTime = endTime - startTime;

            log.info("execute time:" + diffTime + ":" + (diffTime / 1000L) + "초 소요!!!");

//            String callbackUrl = (String) userRequest.get("callbackUrl");

            if(diffTime > (1 * 60 * 1000)) {    //1분초과 선톡
              bFriendTalk = true;
            }

            if(bFriendTalk) {
//                String friendTalkResponse = kakaoService.sendFriendTalkSimpleMessage(receiveId, "(선톡)" + result);
                String friendTalkResponse = kakaoService.sendFriendTalkSimpleMessage(receiveId, result);
                log.info("friendTalkResponse:" + friendTalkResponse);
            } else {
                String callbackResponse = kakaoService.sendCallBack(callbackUrl, kakaoResponse);
                log.info("callbackUrl:" + callbackResponse);
            }

            bSendResponse = true;

//            if(diffTime > (1 * 60 * 1000)) {    //1분초과 선톡
//                //일정 시간 지나면 선톡으로 날려라
//                log.info("1분 초과 선톡 고고:" + diffTime);
//                String friendTalkResponse = kakaoService.sendFriendTalkSimpleMessage(receiveId, "(선톡)" + result);
//                log.info("friendTalkResponse:" + friendTalkResponse);
//            } else if (diffTime > (5 * 1000)) { //5초 초과 콜백
//                log.info("5초 초과 콜백:" + diffTime);
//                log.info("callbackUrl:" + callbackUrl);
//                String callbackResponse = kakaoService.sendCallBack(callbackUrl, kakaoResponse);
//                log.info("callbackUrl:" + callbackResponse);
//            } else {
//                log.info("5초이내 정상처리!!!");
//                String callbackResponse = kakaoService.sendCallBack(callbackUrl, kakaoResponse);
//            }


        } catch (Exception e) {

            if(bFriendTalk && callbackUrl == null || callbackUrl.isBlank()) {
                String friendTalkResponse = kakaoService.sendFriendTalkSimpleMessage(receiveId, "오류가 발생 했습니다. 잠시후 다시 질문해 주세요.");
                log.info("friendTalkResponse:" + friendTalkResponse);
            } else {
                kakaoResponse.setOutputSimpleText("오류가 발생 했습니다. 잠시후 다시 질문해 주세요.");
                String callbackResponse = kakaoService.sendCallBack(callbackUrl, kakaoResponse);
                log.info("callbackUrl:" + callbackResponse);
            }

            throw new RuntimeException(e);
        }

//        return BaseResponse.success(result);
//        return kakaoResponse;
    }
}
