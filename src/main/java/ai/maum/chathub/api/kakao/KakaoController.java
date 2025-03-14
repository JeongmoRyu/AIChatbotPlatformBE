package ai.maum.chathub.api.kakao;

import ai.maum.chathub.api.admin.entity.TempUserMappingEntity;
import ai.maum.chathub.api.admin.service.TempUserMappingService;
import ai.maum.chathub.api.kakao.dto.KakaoResponse;
import ai.maum.chathub.api.kakao.service.KakaoAsyncService;
import ai.maum.chathub.api.kakao.service.KakaoService;
import ai.maum.chathub.api.chat.dto.ChatMessage;
import ai.maum.chathub.api.chatbot.service.ChatbotService;
import ai.maum.chathub.api.chatbotInfo.service.ChatbotInfoService;
import ai.maum.chathub.api.chatroom.entity.ChatroomEntity;
import ai.maum.chathub.api.chatroom.service.ChatroomService;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.service.MemberService;
import ai.maum.chathub.api.skins.service.CallSkinsService;
import ai.maum.chathub.api.kakao.dto.KakaoRequest;
import ai.maum.chathub.mybatis.vo.ChatroomVO;
import ai.maum.chathub.util.DateUtil;
import ai.maum.chathub.util.ObjectMapperUtil;
import ai.maum.chathub.util.WebClientUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name="외부API(카카오)", description="외부 API(카카오)")
@RequestMapping("/extapi/kakao")
public class KakaoController {
    @Value("${service.kakao.chatbot-id}")
    private Long KAKAO_CHATBOT_ID;

    //    private Long startDate = 1717974000000L;   //6월 10일 08시
    private Long startDate = 1717977600000L;   //6월 10일 09시

    private Long[] betaTester = {
             8293L
            ,8253L
            ,8248L
            ,8241L
            ,8214L
            ,8131L
            ,8129L
            ,8112L
            ,8089L
            ,8010L
            ,7997L
            ,7961L
            ,7960L
            ,7959L
            ,7893L
            ,7888L
            ,7854L
            ,7772L
            ,7759L
            ,7752L
            ,7742L
            ,7718L
            ,7663L
            ,7632L
            ,7609L
            ,7588L
            ,7587L
            ,6810L
            ,6517L
            ,4338L
            ,2116L
//            ,10000000020L
    };
    private boolean callBackYn = false;
    private String responseType = "SIMPLEIMAGE";
    private String responseImageUrl = "https://url/img.jpg";
    private String responseTextMappinged = "지금부터 베타테스트를 시작합니다☺ 귀한 시간을 함께해 주셔서 감사합니다!\n2주 동안 상담 AI 서비스를 자유롭게 이용해 주시고, 테스트 기간 후에는 설문조사를 통해 상세한 피드백을 요청드릴 예정입니다.\n아모레 시티랩 AI가 고객님의 피부관리에 도움이 되도록 하겠습니다\uD83D\uDC95";

    private String responseTextNotMappinged = "원활한 챗봇 사용을 위해 환경 세팅을 마무리해 주세요!\uD83D\uDC40";

    private final ChatbotService chatbotService;
    private final ChatroomService chatroomService;
    private final MemberService memberService;
    private final KakaoService kakaoService;
    private final KakaoAsyncService kakaoAsyncService;
    private final CallSkinsService callSkinsService;
    private final TempUserMappingService tempUserMappingService;
    private final ChatbotInfoService chatbotInfoService;
    private final String[] AwatingMent = {
            "🔍 답변을 검토 중이에요. 잠시만 기다려주세요!",
            "답변을 검토하고 있어요. 잠시만 기다려주세요!👀",
            "{name}님! 보내주신 톡을 확인했어요. 잠시만 기다려주세요!🏃‍♀",
            "⏳ 보다 정확한 답변을 위해 답장까지 다소 시간이 소요됩니다. 최대한 빨리 답장 드릴게요!",
            "🔍 자료를 검토 중이에요! 곧 답변을 보내드릴 테니 잠시만 기다려주세요☺",
            "자료를 확인하고 있습니다. 잠시만 기다려주세요!👀",
            "{name}님께 보다 정확한 답변을 드리기 위해 자료를 검토 중이에요. 잠시만 기다려주세요!☺",
            "{name}님께 맞춤인 답변을 정리 중이에요✒ 잠시만 기다려주세요!",
            "{name}님을 위한 맞춤 답변을 작성하고 있어요✒ 잠시만 기다려주세요!"
    };

    @PostMapping("/set/response")
    public Map<String, Object> setResponseData(
            @RequestParam(name="oktext", required=false) String oktext,
            @RequestParam(name="notoktext", required=false) String notoktext,
            @RequestParam(name="image", required=false) String image,
            @RequestParam(name="callback", required=false) Boolean callback,
            @RequestParam(name="type", required=false) String type,
            @RequestParam(name="startdt", required=false) Long startDt,
//            @RequestBody(required=false) Long[] tester
            @RequestBody(required=false) ArrayList<Long> tester
    ) {
        if(oktext != null && !oktext.isBlank())
            this.responseTextMappinged = oktext;
        if(notoktext != null && !notoktext.isBlank())
            this.responseTextNotMappinged = notoktext;
        if(image != null && !image.isBlank())
            this.responseImageUrl = image;
        if(callback != null)
            this.callBackYn = callback;
        if(type != null && !type.isBlank())
            this.responseType = type;
        if(startDt != null)
            this.startDate = startDt;
        if(tester != null && tester.size() > 0) {
            try {
                this.betaTester = tester.toArray(new Long[tester.size()]);
            } catch (Exception e) {
                log.error("Beta Test Set Error:" + e.getMessage());
            }
        }

        Map<String, Object> rtnMap = new HashMap<>();
        rtnMap.put("type", this.responseType);
        rtnMap.put("oktext", this.responseTextMappinged);
        rtnMap.put("notoktext", this.responseTextNotMappinged);
        rtnMap.put("image", this.responseImageUrl);
        rtnMap.put("callback", this.callBackYn);
        rtnMap.put("startDt", this.startDate);
        rtnMap.put("startDtFormat", DateUtil.convertToFGTStringByMS(this.startDate));
        rtnMap.put("tester", this.betaTester);
        return rtnMap;
    }

    @PostMapping("/user/test/token")
    public Map<String, String> testGetKakaoUser(
            @RequestBody LinkedHashMap requestObject
    ) {
        log.info("request Object:" + ObjectMapperUtil.writeValueAsString(requestObject));
        Map<String, String> response = new HashMap<String,String>();
        response.put("app_user_id", "userid");
        response.put("phone_number", "phone_number");
        return response;
    }

    @PostMapping("/user/sync")
    public Map<String, Object> sync(
            @RequestBody LinkedHashMap request
    ) {

        try {

            Map<String, Map<String, String>> mappingData = new HashMap<>();

            log.info("request Object:" + ObjectMapperUtil.writeValueAsString(request));

            LinkedHashMap actionObject = (LinkedHashMap) request.get("action");
            LinkedHashMap detailParamsObject = (LinkedHashMap) actionObject.get("detailParams");
            LinkedHashMap profileInfoObject = (LinkedHashMap) detailParamsObject.get("profileInfo");
            LinkedHashMap valueObject = (LinkedHashMap) profileInfoObject.get("value");


            LinkedHashMap userRequestObject = (LinkedHashMap) request.get("userRequest");
            LinkedHashMap userObject = (LinkedHashMap) userRequestObject.get("user");
            String botUserKey = (String) userObject.get("id");


            String callUrl = (String) valueObject.get("otp") + "?rest_api_key=apiKeyValue";

            log.info("callUrl :" + callUrl);

            String responseString = WebClientUtil.callUrl(HttpMethod.GET, callUrl);

            log.info("responseString :" + responseString);
            Map<String, Object> responseObject = ObjectMapperUtil.readValue(responseString, Map.class);

            log.info("responseObject :" + ObjectMapperUtil.writeValueAsString(responseObject));

            String appUserId = (String) responseObject.get("app_user_id");
            String phoneNumber = (String) responseObject.get("phone_number");

//            KakaoUserEntity kakaoUserEntity = new KakaoUserEntity(botUserKey, appUserId, phoneNumber);

            log.info("BotUserKey: " + botUserKey);
            log.info("AppUserKey: " + appUserId);
            log.info("PhoneNumber: " + phoneNumber);

            if (!mappingData.containsKey(botUserKey)) {
                mappingData.put(botUserKey, new HashMap<>());
            }
            mappingData.get(botUserKey).put("appUserId", appUserId);
            mappingData.get(botUserKey).put("phoneNumber", phoneNumber);

            log.info("MappingData: " + mappingData);

            Map<String, Object> response = new HashMap<>();
            response.put("version", "2.0");
            Map<String, Object> template = new HashMap<>();
            Map<String, Object> simpleText = new HashMap<>();
            simpleText.put("text", "appUserId: " + appUserId + "\nphoneNumber: " + phoneNumber);
            template.put("outputs", new Object[]{simpleText});
            response.put("template", template);

            log.info("response Object (Success):" + ObjectMapperUtil.writeValueAsString(response));
            return response;
        } catch (Exception e) {
            log.error("KAKAO SYNC ERROR:" + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("version", "2.0");
            Map<String, Object> template = new HashMap<>();
            Map<String, Object> simpleText = new HashMap<>();
            simpleText.put("text", "오류가 발생했습니다.");
            template.put("outputs", new Object[]{simpleText});
            response.put("template", template);

            log.info("response Object (error):" + ObjectMapperUtil.writeValueAsString(response));
            return response;
        }
    }

    @Operation(summary = "채팅", description = "채팅")
    @PostMapping({"/chat"})
    public KakaoResponse chat(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody @Parameter(name = "카카오Param", required = false) LinkedHashMap paramObject,
            @RequestParam(name="message", required=false) @Parameter(name = "채팅메시지", required = true) String message
    ) {

        //카카오에서 어떤 형태로 호출이되는지 알아보기 위해 Object형으로 받아서 String으로 변환해서 로깅해봄.

        Long startTime = System.currentTimeMillis();

        String jsonString = "";

        try {
            jsonString = String.valueOf(ObjectMapperUtil.writeValueAsString(paramObject));
            log.info("kakaoParam:" + jsonString);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        log.info("vendor:" + user.getUsername());
        log.info("chat:" + message);
        KakaoResponse kakaoResponse = new KakaoResponse();
        kakaoResponse.setUseCallback(true);

        //테스트코드
//        kakaoResponse.setTextResponse("test!!");
//        log.debug(ObjectMapperUtil.writeValueAsString(kakaoResponse));

        KakaoRequest kakaoRequest = ObjectMapperUtil.readValue(jsonString, KakaoRequest.class);

        LinkedHashMap userRequest = (LinkedHashMap) paramObject.get("userRequest");
        LinkedHashMap kakaoUser = (LinkedHashMap) userRequest.get("user");
        LinkedHashMap properties = (LinkedHashMap) kakaoUser.get("properties");
        String botUserKey = (String) properties.get("botUserKey");
        String appUserId = (String) properties.get("appUserId");

        log.info("botUserKey:" + botUserKey + ":appUserId:" + appUserId);

        MemberDetail member = memberService.loadUserByVendorIdAndVendorType(botUserKey, "KAKAO");
        if(member == null)
            member = memberService.findMemberByVendorUserIdAndVendorType(appUserId, "KAKAO");

        //kakaoParam이 유입 되었을때는 message를 kakaoParam에 있는 놈으로 사용.

        try {
            if (paramObject != null) {
                message = (String) userRequest.get("utterance");
            }
        } catch (Exception e) {
        }

        if (message == null || message.isBlank()) {
            kakaoResponse.setDataText("채팅메시지가 없습니다.");
            return kakaoResponse;
        }


        if(member == null) {
     
            //매핑되지 않은 사용자 체크로직
            //이름을 입력 했을때, 이름이 일치 하면 자동 매핑 시켜줌. FGT를 위한 임시 로직.

            //일단 메시지를 임시 테이블에 넣어준다 (추후 확인용)
            tempUserMappingService.setTempMessageEntity(botUserKey, message);

            TempUserMappingEntity tempUser = tempUserMappingService.getTempUserMappingWithUserName(message.trim());

            String responseText = "";

            responseText = this.responseTextNotMappinged;

            //tempUser가 있으면 memeber 테이블에 봇ID 업데이트
            if(tempUser !=null) {
                int result = memberService.updateMemberBotId(tempUser.getUserKey(), botUserKey);
                if (result > 0) { //성공
                    log.info("------------ CHATBOT KEY MAPPING---------------:mapping OK:" + message + ":" + tempUser.getUserKey() + ":" + botUserKey);
                    responseText = this.responseTextMappinged.replaceAll("\\{name\\}", message);
                    responseText = responseText.replaceAll("\\{startDate\\}", DateUtil.convertToFGTStringByMS(startDate));
                    responseType = "SIMPLETEXT";
                } else {
                    log.info("------------ CHATBOT KEY MAPPING---------------:mapping FAIL:" + message + ":" + tempUser.getUserKey() + ":" + botUserKey);
                    responseType = "SIMPLEIMAGE";
                }
            }

            kakaoResponse.setUseCallback(callBackYn);

            if(responseType.toUpperCase().equals("TEXT")) {
                kakaoResponse.setDataText(responseText);
            }
            else if(responseType.toUpperCase().equals("SIMPLETEXT")) {
                kakaoResponse.setOutputSimpleText(responseText);
            }
            else if(responseType.toUpperCase().equals("SIMPLEIMAGE")) {
                kakaoResponse.setOutputSimpleImage(responseImageUrl,responseText);
            }

            log.info("------------ CHATBOT KEY MAPPING---------------:response:" + ObjectMapperUtil.writeValueAsString(kakaoResponse));

            return kakaoResponse;
        }

//        String memberId = member.getUsername();
        String memberId = String.valueOf(member.getUserKey());
        String receiveId = member.getReceiveId();
        String memberName = member.getName();

        log.info("userKey:" + memberId);

        //FGT 시작 체크
        //베타 테스터이고 현재일시가 시작 일시보다 이른 시간 이면>
        Long currentTime = System.currentTimeMillis();
        log.info("BETA TESTER CHECK:" + memberId + ":" + this.startDate + ":" + DateUtil.convertToFGTStringByMS(this.startDate) + ":" + currentTime + ":" + DateUtil.convertToFGTStringByMS(currentTime));
        if(this.startDate > currentTime && Arrays.stream(this.betaTester).anyMatch(value -> memberId.equals(String.valueOf(value)))) {
            String responseText = this.responseTextMappinged.replaceAll("\\{name\\}", memberName);
            responseText = responseText.replaceAll("\\{startDate\\}", DateUtil.convertToFGTStringByMS(startDate));
            responseType = "SIMPLETEXT";
            kakaoResponse.setOutputSimpleText(responseText);
            kakaoResponse.setUseCallback(false);
            return kakaoResponse;
        }

        //카카오채널용 챗봇 ID - ChatbotID 1로 FIX - 나중엔 따로 관리하도록 해야 함...
//        Long chatbotId = 10L;
        Long chatbotId = KAKAO_CHATBOT_ID;

//        String chatbotIdString = String.valueOf(member.getUserKey()).substring(String.valueOf(member.getUserKey()).length() - 2);
//        chatbotId = Long.valueOf(chatbotIdString);

        log.info("chatbotId:" + chatbotId);

//        ChatbotVO chatbot = chatbotService.getChatbotByIdWithMapper(chatbotId);
//        List<HashMap<String, Object>> chatbotInfo = chatbotInfoService.getChatbotInfoMap(chatbotId);
        ChatMessage chatMessage = new ChatMessage("user", message);

        //seq는 timestamp 기반으로 생성해준다.
        Long seq = System.currentTimeMillis();
        chatMessage.setSeq(seq);

        log.info("create message sequence:" + seq);

        List<ChatMessage> messages = new ArrayList<ChatMessage>();
        messages.add(chatMessage);
        String chatroomRegUserId = "KAKAOCHAT_" + member.getUsername(); //외부채널용 chatroom은 채널Type + 사용자ID로 정의.
        //chatroom이 없으면 생성해주는 코드
        ChatroomVO chatroomVO = chatroomService.getChatroomByRegUserID(chatroomRegUserId);
        Long chatroomId = chatroomVO == null ? null : chatroomVO.getId();

        if (chatroomVO == null || chatroomId == null) {  //챗룸은 사용자당 하나씩만 만들어짐. 챗룸이 없으면 신규 생성.
            ChatroomEntity chatroom = new ChatroomEntity();
            chatroom.setTitle("카카오챗:" + member.getUsername() + ":" + member.getName());
            chatroom.setChatbotId(chatbotId);
            chatroom.setRegUserId(chatroomRegUserId);
            chatroom = chatroomService.setChatroom(chatroom);
            chatroomId = chatroom.getId();
            if (chatroom == null) {
                kakaoResponse.setDataText("죄송합니다. 지금은 대화 할 수 없습니다. 잠시후 시도해 주세요");
            }
        }

        //5초 넘을때의 답변을 일단 먼저 보내준다.
        int index = new Random().nextInt(AwatingMent.length);
        String ment = AwatingMent[index].replaceAll("\\{name\\}", memberName);
        kakaoResponse.setDataText(ment);

        log.info("Kakao Chat Response:" + ObjectMapperUtil.writeValueAsString(kakaoResponse));

        kakaoAsyncService.afterChat(chatbotId, chatroomId, messages, memberId, startTime, receiveId, userRequest, member);
//        callSkinsService.callSkins("/extapi/member/chattime/" + member.getUsername(), HttpMethod.POST);

        return kakaoResponse;
    }


    @Operation(summary = "콜백테스트", description = "")
    @PostMapping({"/test/callback"})
    @ResponseBody
    public String callBackTest(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody @Parameter(name = "카카오Param", required = false) LinkedHashMap paramObject,
            @RequestParam(name="message", required=false) @Parameter(name = "채팅메시지", required = true) String message
    ) {

        String temp1 = ObjectMapperUtil.writeValueAsString(paramObject);
        KakaoResponse kakaoResonse = ObjectMapperUtil.readValue(temp1, KakaoResponse.class);
        String result = kakaoService.sendCallBack("url/callback/url", kakaoResonse);
        return result;
    }

}
