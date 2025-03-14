package ai.maum.chathub.api.kakao.service;

import ai.maum.chathub.api.kakao.dto.*;
import ai.maum.chathub.api.chatroom.entity.ChatroomDetailEntity;
import ai.maum.chathub.api.chatroom.service.ChatroomService;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.mapper.MemberMapper;
import ai.maum.chathub.api.routine.entity.RoutineDetailEntity;
import ai.maum.chathub.api.routine.entity.RoutineInfoEntity;
import ai.maum.chathub.api.routine.service.RoutineService;
import ai.maum.chathub.mybatis.vo.ChatroomVO;
import ai.maum.chathub.util.DateUtil;
import ai.maum.chathub.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoService {

    private final RoutineService routineService;
    private final RestTemplate restTemplate;
//    private final KakaoUserRepository kakaoUserRepository;
    private final MemberMapper memberMapper;
    private final ChatroomService chatroomService;

    @Value("${service.kakao.channel.url}")
    private String kakaoApiUrl;

    @Value("${service.kakao.channel.id}")
    private String clientId;

    @Value("${service.kakao.channel.secret}")
    private String clientSecret;

    @Value("${service.kakao.channel.senderkey}")
    private String senderKey;

    @Value("${service.kakao.channel.senderno}")
    private String senderNo;

    private String accessToken;

    private Long expireTime;

//    public KakaoUserEntity getKakaoUser(String botUserId) {
//        return kakaoUserRepository.getReferenceById(botUserId);
//    }

//    public KakaoUserEntity setKakaoUser(KakaoUserEntity kakaoUserEntity) {
//        return kakaoUserRepository.save(kakaoUserEntity);
//    }

    public String getAccessToken() {
        log.info("expireTime:" + String.valueOf(this.expireTime==null?"null":DateUtil.convertToStringByMs(this.expireTime)));
        log.info("currentTime:" + DateUtil.convertToStringByMs(System.currentTimeMillis()));
        if(this.accessToken == null || this.expireTime == null || this.expireTime < System.currentTimeMillis()) {
            Boolean bSuccess = oAuth();
            if(!bSuccess)
                return null;
        }
        return this.accessToken;
    }
    public Boolean oAuth () {

        Boolean bSuccess = false;

        try {

            log.info("oAuth id:" + clientId);
            log.info("oAuth secret:" + clientSecret);

            String apiUrl = kakaoApiUrl + "/v1/oauth/token";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", String.format("Basic %s %s", clientId, clientSecret));
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("url:" + apiUrl + ":entity:" + ObjectMapperUtil.writeValueAsString(entity));
            // GET 요청 보내기
            ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            String strBody = responseEntity.getBody();

            log.info("result:" + strBody);

            AuthResponse response = ObjectMapperUtil.readValue(strBody, AuthResponse.class);

            if ("API_200".equals(response.getCode()) && !response.getAccessToken().isBlank()) {
                this.accessToken = response.getAccessToken();
                this.expireTime = System.currentTimeMillis() + (response.getExpiresIn()) * 1000 - 5 * 60 * 1000;
                log.info("expireTime:" + DateUtil.convertToStringByMs(expireTime));
                bSuccess = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return bSuccess;
    }

//    public String sendCallBack(String callbackUrl, KakaoRequest kakaoRequest) {
    public String sendCallBack(String callbackUrl, KakaoResponse kakaoResonse) {
        String accessToken = getAccessToken();
//        String response = ObjectMapperUtil.writeValueAsString(kakaoRequest);
        String response = "";

        URI uri = null;

        try {
            uri = new URI(callbackUrl);
        } catch (Exception e) {

        }

        String paramObject = ObjectMapperUtil.writeValueAsString(kakaoResonse);

        log.info("Kakao Friend Talk request:" + paramObject);

        /* RestTemplate 사용 */
        try {

//            String apiUrl = kakaoApiUrl + "/v1/oauth/token";

            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", String.format("Basic %s %s", clientId, clientSecret));
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(paramObject, headers);

            log.info("url:" + callbackUrl + ":entity:" + ObjectMapperUtil.writeValueAsString(entity));
            // GET 요청 보내기
            ResponseEntity<String> responseEntity = restTemplate.exchange(callbackUrl, HttpMethod.POST, entity, String.class);

            String strBody = responseEntity.getBody();

            log.info("result:" + strBody);

//            AuthResponse response = ObjectMapperUtil.readValue(strBody, AuthResponse.class);
//
//            if ("API_200".equals(response.getCode()) && !response.getAccessToken().isBlank()) {
//                this.accessToken = response.getAccessToken();
//                this.expireTime = System.currentTimeMillis() + (response.getExpiresIn()) * 1000 - 5 * 60 * 1000;
//                log.info("expireTime:" + DateUtil.convertToStringByMs(expireTime));
//                bSuccess = true;
//            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        /* WebClient 사용 */
        /*
        if(uri != null && kakaoRequestObjct != null) {
            WebClient webClient = WebClient.builder()
//                .baseUrl(callbackUrl)
                    .baseUrl(uri.getScheme() + "://" + uri.getHost())
                    .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader("Accept", MediaType.ALL_VALUE)
//                    .defaultHeader("Authorization", String.format("Bearer %s", accessToken))
                    .build();

            WebClient.RequestBodySpec requestBodySpec = webClient.post()
                    .uri(uri.getPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.ALL);

            WebClient.ResponseSpec responseSpec = requestBodySpec
                    .body(Mono.just(kakaoRequestObjct), String.class)
//                    .body(kakaoRequest, KakaoRequest.class)
                    .retrieve();

            response = responseSpec
                    .bodyToMono(String.class)
                    .block(); // Block to get the response synchronously (for simplicity)

            log.info("Kakao Friend Talk response:" + response);

        }
         */

        return response;

    }

    public String sendFriendTalkSimpleMessage(String receiveId, String message) {
        if(receiveId == null || receiveId.isBlank())
            receiveId = "01063324347";

        log.info("friendTalkSend..1.:" + receiveId + ":" + message);

        FriendTalkRequest friendTalkRequest = new FriendTalkRequest();
        friendTalkRequest.setMessageType("FT");
        friendTalkRequest.setSenderKey(senderKey);
        friendTalkRequest.setPhoneNumber(receiveId);
        friendTalkRequest.setSenderNo(senderNo);
        friendTalkRequest.setAdFlag("N");
        friendTalkRequest.setMessage(message);

        log.info("friendTalkSend..2.:" + receiveId + ":" + message);

        String apiUrl = kakaoApiUrl + "/v1/ft/send";
        String accessToken = getAccessToken();

        log.info("friendTalkSend..3.:" + receiveId + ":" + message);

        WebClient webClient = WebClient.builder()
                .baseUrl(kakaoApiUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.ALL_VALUE)
                .defaultHeader("Authorization", String.format("Bearer %s", accessToken))
                .build();

        log.info("friendTalkSend..4.:" + receiveId + ":" + message);

        WebClient.RequestBodySpec requestBodySpec = webClient.post()
                .uri("/v1/ft/send")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL);

        log.info("friendTalkRequest:" + ObjectMapperUtil.writeValueAsString(friendTalkRequest));

        WebClient.ResponseSpec responseSpec = requestBodySpec
                .body(Mono.just(ObjectMapperUtil.writeValueAsString(friendTalkRequest)), String.class)
                .retrieve();

        String response = responseSpec
                .bodyToMono(String.class)
                .block(); // Block to get the response synchronously (for simplicity)

        log.info("Kakao Friend Talk response:" + response);


        return response;
    }

    public BaseResponse sendFriendTalk(String taskCd, String phoneNumber) {

        LinkedHashMap<String, Object> responseObject;
        responseObject = new LinkedHashMap<>();

        try {
            RoutineInfoEntity routineInfo = routineService.getRoutineInfo(taskCd);

            if (routineInfo == null) {
                return BaseResponse.failure("테스크가 없습니다:" + taskCd);
            }
            List<RoutineDetailEntity> routineDetails = routineService.getRoutineDetails(taskCd);

            String messageType = "FT";
            Image image = null;
            List<Button> buttons = new ArrayList<Button>();

            for (RoutineDetailEntity routineDetail : routineDetails) {
                switch (routineDetail.getType()) {
                    case ("BUTTON"):
                        buttons.add(new Button(routineDetail.getText(), "BK", "", ""));
                        break;
                    case ("WBUTTON"):    //웹링크버튼
                        if (routineDetail.getLink().isBlank())
                            buttons.add(new Button(routineDetail.getText(), "MD", "", ""));
                        else
                            buttons.add(new Button(routineDetail.getText(), "WL", routineDetail.getLink(), routineDetail.getLinkPc()));
                        break;
                    case ("ABUTTON"):   //앱링크버튼
                        buttons.add(new Button(routineDetail.getText(), "AL", routineDetail.getLink(), routineDetail.getLinkPc()));
                        break;
                    case ("IMAGE"):
                        image = new Image(routineDetail.getText(), routineDetail.getLink());
                        messageType = "FI";
                        break;
                    case ("WIMAGE"):
                        image = new Image(routineDetail.getText(), routineDetail.getLink());
                        messageType = "FW";
                        break;
                }
            }

            MemberDetail member = memberMapper.findMemberByReceiveIdAndVendorType(phoneNumber, "KAKAO");

            String senarioMent = routineInfo.getSenarioMent();
            //랜덤여부 체크
            if ("Y".equals(routineInfo.getRandomYn())) {
                try {
                    JSONArray array = new JSONArray(senarioMent);
                    JSONObject obj = (JSONObject) array.get(new Random().nextInt(array.length()));
                    senarioMent = obj.getString("senario_ment") == null || obj.getString("senario_ment").isBlank() ? senarioMent : obj.getString("senario_ment");
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }

            String memberName = "";
            //이름 넣어 줌
            if (member != null && !member.getName().isBlank())
                memberName = member.getName();
            else
                memberName = "고객";
            senarioMent = senarioMent.replaceAll("\\{name\\}", member.getName());

            //광고여부
            String adFlag = "N";
            if("Y".equals(routineInfo.getAdYn()))
                adFlag = "Y";

            FriendTalkRequest friendTalkRequest = new FriendTalkRequest();
            friendTalkRequest.setMessageType(messageType);
            friendTalkRequest.setSenderKey(senderKey);
            friendTalkRequest.setPhoneNumber(phoneNumber);
            friendTalkRequest.setSenderNo(senderNo);
            friendTalkRequest.setMessage(senarioMent);
            friendTalkRequest.setAdFlag(adFlag);
            friendTalkRequest.setButton(buttons);
            if (image != null && (messageType.equals("FI") || messageType.equals("FW")))
                friendTalkRequest.setImage(image);

            String apiUrl = kakaoApiUrl + "/v1/ft/send";
            String accessToken = getAccessToken();

            WebClient webClient = WebClient.builder()
                    .baseUrl(kakaoApiUrl)
                    .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader("Accept", MediaType.ALL_VALUE)
                    .defaultHeader("Authorization", String.format("Bearer %s", accessToken))
                    .build();

            WebClient.RequestBodySpec requestBodySpec = webClient.post()
                    .uri("/v1/ft/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.ALL);

            log.debug("friendTalkRequest:" + ObjectMapperUtil.writeValueAsString(friendTalkRequest));

            WebClient.ResponseSpec responseSpec = requestBodySpec
                    .body(Mono.just(ObjectMapperUtil.writeValueAsString(friendTalkRequest)), String.class)
                    .retrieve();

            String response = responseSpec
                    .bodyToMono(String.class)
                    .block(); // Block to get the response synchronously (for simplicity)

            log.info("Kakao Friend Talk response:" + response);

            //
            try {
                responseObject = ObjectMapperUtil.readValue(response, LinkedHashMap.class);
//                JSONObject responseObject = ObjectMapperUtil.readValue(response, JSONObject.class);
//                String resultData = (String) responseObject.get("data");
//                JSONObject dataObject = ObjectMapperUtil.readValue(resultData, JSONObject.class);
                String resultCode = (String) responseObject.get("message");
                if ("성공".equals(resultCode)) {
                    String regUserId = "KAKAOCHAT_" + String.valueOf(member.getUserKey());
                    ChatroomVO chatroomEntity = chatroomService.getChatroomByRegUserID(regUserId);
                    ChatroomDetailEntity chatroomDetailEntity = new ChatroomDetailEntity();
                    if (chatroomEntity != null) {
                        //히스토리는 QA쌍으로 들어가야 한다.
                        //그래서 user 멘트를 빈값으로 넣어 둠.
                        Long seq = System.currentTimeMillis();
                        chatroomDetailEntity = new ChatroomDetailEntity(chatroomEntity.getId(), seq, "user", ".");
                        chatroomService.setChatroomDetail(chatroomDetailEntity);
                        chatroomDetailEntity = new ChatroomDetailEntity(chatroomEntity.getId(), seq, "assistant", senarioMent);
                        chatroomService.setChatroomDetail(chatroomDetailEntity);
                    }

                    if(routineInfo.getNextTaskCd() != null && !routineInfo.getNextTaskCd().isBlank()) {
                        BaseResponse baseResponse = sendFriendTalk(routineInfo.getNextTaskCd(), phoneNumber);
                        responseObject = (LinkedHashMap) baseResponse.getData();
                    }

                    return BaseResponse.success(responseObject);
                } else {
                    return BaseResponse.failure(responseObject, "친구톡 발송 실패");
                }
            } catch (Exception e) {
                log.error("friendTalk response parse error:" + e.getMessage());
                return BaseResponse.failure("friendTalk response parse error:" + e.getMessage());
            }

//            return BaseResponse.success(response);
//            return response;
        } catch (Exception e) {
//            return BaseResponse.failure("친구톡 발송 오류:" + e.getMessage());
            return BaseResponse.failure("friendTalk send error:" + e.getMessage());
        }
/*
        List<Button> buttons = new ArrayList<Button>();

        FriendTalkRequest friendTalkRequest = new FriendTalkRequest();

        friendTalkRequest.setMessageType("FI");
        friendTalkRequest.setSenderKey("4639bf90a9ee65f9a383247bdb34c1fc2ff12896");
        friendTalkRequest.setPhoneNumber("821063324347");
        friendTalkRequest.setSenderNo("0200000000");
        friendTalkRequest.setMessage("안녕하시렵니까?");
        friendTalkRequest.setButton(buttons);
        friendTalkRequest.setImage(image);
*/
//        return BaseResponse.success(responseObject);
    }

    public Boolean sendFriendTalk() {
        Boolean bSuccess = false;

        List<Button> buttons = new ArrayList<Button>();
        buttons.add(new Button("웹링크 버튼 테스트", "WL","https://www.naver.com", "https://comic.naver.com"));
        buttons.add(new Button("앱링크 버튼 테스트", "AL","kakaomap://open", "kakaomap://open"));
        buttons.add(new Button("메시지 전달 테스트", "MD","", ""));

        Image image = new Image("https://mud-kage.kakao.com/dn/csY0ye/btses5OP5xN/dSTdYeB0aNUrhz0SOX3WK1/img_l.jpg", "https://comic.naver.com");

        FriendTalkRequest friendTalkRequest = new FriendTalkRequest();
        friendTalkRequest.setMessageType("FI");
        friendTalkRequest.setSenderKey(senderKey);
        friendTalkRequest.setPhoneNumber("821063324347");
        friendTalkRequest.setSenderNo(senderNo);
        friendTalkRequest.setMessage("안녕하시렵니까?");
        friendTalkRequest.setAdFlag("N");
        friendTalkRequest.setButton(buttons);
        friendTalkRequest.setImage(image);

        String apiUrl = kakaoApiUrl + "/v1/ft/send";
        String accessToken = getAccessToken();

        // 요청 헤더 설정
        /*
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", accessToken));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.ALL));

        HttpEntity<FriendTalkRequest> entity = new HttpEntity<>(friendTalkRequest, headers);

        log.debug("friendTalkRequest:" + ObjectMapperUtil.writeValueAsString(entity));

        // GET 요청 보내기
        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

        String strBody = responseEntity.getBody();

        log.debug("kakao friendtalk send" + strBody);
*/

        WebClient webClient = WebClient.builder()
                .baseUrl(kakaoApiUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.ALL_VALUE)
                .defaultHeader("Authorization", String.format("Bearer %s", accessToken))
                .build();

//        YourObject yourObject = new YourObject();
        // Set properties of yourObject

        WebClient.RequestBodySpec requestBodySpec = webClient.post()
                .uri("/v1/ft/send")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL);

        WebClient.ResponseSpec responseSpec = requestBodySpec
                .body(Mono.just(ObjectMapperUtil.writeValueAsString(friendTalkRequest)), String.class)
                .retrieve();

        String response = responseSpec
                .bodyToMono(String.class)
                .block(); // Block to get the response synchronously (for simplicity)

        System.out.println(response);

        return bSuccess;
    }

    public String sendFriendTalkTest(String phoneNumber, String text, List<RoutineDetailEntity> routineDetails ) {
        if(phoneNumber == null || phoneNumber.isBlank())
            phoneNumber = "01063324347";

        log.info("sendFriendTalkTest..1.:" + phoneNumber + ":" + text);

        Image image = null;
        List<Button> buttons = new ArrayList<Button>();
        String messageType = "FT";

        for(RoutineDetailEntity routineDetail: routineDetails) {
            String type = routineDetail.getType();
            switch (type) {
                case("BUTTON"):
                    buttons.add(new Button(routineDetail.getText(), "BK","", ""));
                    break;
                case ("WBUTTON"):    //웹링크버튼
                    if(routineDetail.getLink().isBlank())
                        buttons.add(new Button(routineDetail.getText(), "MD","", ""));
                    else
                        buttons.add(new Button(routineDetail.getText(), "WL", routineDetail.getLink(), routineDetail.getLinkPc()));
                    break;
                case ("ABUTTON"):   //앱링크버튼
                    buttons.add(new Button(routineDetail.getText(), "AL", routineDetail.getLink(), routineDetail.getLinkPc()));
                    break;
                case ("IMAGE"):
                    image = new Image(routineDetail.getText(), routineDetail.getLink());
                    messageType = "FI";
                    break;
                case ("WIMAGE"):
                    image = new Image(routineDetail.getText(), routineDetail.getLink());
                    messageType = "FW";
                    break;
            }
        }

        FriendTalkRequest friendTalkRequest = new FriendTalkRequest();
        friendTalkRequest.setMessageType(messageType);
        friendTalkRequest.setSenderKey(senderKey);
        friendTalkRequest.setPhoneNumber(phoneNumber);
        friendTalkRequest.setSenderNo(senderNo);
        friendTalkRequest.setMessage(text);
        friendTalkRequest.setAdFlag("N");
        friendTalkRequest.setButton(buttons);
        if(image != null && (messageType.equals("FI") || messageType.equals("FW")))
            friendTalkRequest.setImage(image);

        String apiUrl = kakaoApiUrl + "/v1/ft/send";
        String accessToken = getAccessToken();

        WebClient webClient = WebClient.builder()
                .baseUrl(kakaoApiUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.ALL_VALUE)
                .defaultHeader("Authorization", String.format("Bearer %s", accessToken))
                .build();

        WebClient.RequestBodySpec requestBodySpec = webClient.post()
                .uri("/v1/ft/send")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL);

        log.debug("friendTalkRequest:" + ObjectMapperUtil.writeValueAsString(friendTalkRequest));

        WebClient.ResponseSpec responseSpec = requestBodySpec
                .body(Mono.just(ObjectMapperUtil.writeValueAsString(friendTalkRequest)), String.class)
                .retrieve();

        String response = responseSpec
                .bodyToMono(String.class)
                .block(); // Block to get the response synchronously (for simplicity)

        log.info("sendFriendTalkTest response:" + response);

        return response;
    }
}
