package ai.maum.chathub.api.chatplay.service;

import ai.maum.chathub.api.auth.service.AuthService;
import ai.maum.chathub.api.chatplay.dto.req.ChatplayReq;
import ai.maum.chathub.api.chatplay.dto.req.ChatplayUserInfoReq;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.service.MemberService;
import ai.maum.chathub.meta.ResponseMeta;
import ai.maum.chathub.util.JwtUtil;
import ai.maum.chathub.util.ObjectMapperUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.LinkedHashMap;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatplayService {
    @Value("${service.chatplay.admin-id}")
    private Long ADMIN_ID;
    @Value("${service.chatplay.url}")
    private String API_URL;
    @Value("${service.chatplay.key}")
    private String API_KEY;
    private final RestTemplate restTemplate;
    private final MemberService memberService;

//    public BaseResponse<Void> processUser(List<ChatplayReq> userList, HttpMethod method, String authorizationHeader) {
    public BaseResponse<Void> processUser(Object users, HttpMethod method, String authorizationHeader) {
        if(users == null) {
            return BaseResponse.failure(ResponseMeta.PARAM_WRONG);
        }
        String url = API_URL + "/user";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Vendor-ID", API_KEY);

        if(authorizationHeader == null || authorizationHeader.isEmpty()) {
            //authorizationHeader가 없으면 ADMIN으로 진행.
            MemberDetail userDetails = memberService.findMemberByUserKey(ADMIN_ID);
            String userId = getChatplayId(userDetails.getUsername());
            userDetails.setUserId(userId);
            authorizationHeader = "Bearer " + JwtUtil.generateToken(userDetails);
        }

        headers.add("Authorization", authorizationHeader);
        headers.add("Content-Type", "application/json"); // 필요에 따라 추가

        HttpEntity<?> requestEntity = null;
//        HttpEntity<Object> requestEntity = new HttpEntity<>(users, headers);
        BaseResponse<Void> baseResponse;

        if (users instanceof List) {
            // Check the type of elements in the List
            List<?> userList = (List<?>) users;

            if (!userList.isEmpty() && userList.get(0) instanceof String) {
                requestEntity = new HttpEntity<>((List<String>) users, headers);
            } else if (!userList.isEmpty() && userList.get(0) instanceof ChatplayReq) {
                requestEntity = new HttpEntity<>((List<ChatplayReq>) users, headers);
            }
//            } else {
//                throw new IllegalArgumentException("Invalid List type in users");
//            }
        } else if (users instanceof ChatplayReq) {
            requestEntity = new HttpEntity<>((ChatplayReq) users, headers);
        }
//        else {
//            throw new IllegalArgumentException("Invalid type for users");
//        }

        try {

            ResponseEntity<String> response = restTemplate.exchange(url, method, requestEntity, String.class);

            try {
                baseResponse = ObjectMapperUtil.readValue(response.getBody(), BaseResponse.class);
                log.info("Chatplay registUser response - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            } catch (Exception e) {
                // JSON 파싱 실패 시 (즉, 응답이 BaseResponse 형식이 아닐 때)
                log.warn("Unexpected response format. Raw response: {}", response.getBody());

                // 적절한 오류 메시지를 담은 BaseResponse 생성
                baseResponse = BaseResponse.failure(ResponseMeta.FAILURE);
                // 예: return baseResponse;
            }
        } catch (Exception e) {
            log.error("Error occurred while registering user: ", e);
            // API 호출 자체가 실패했을 때의 처리
            baseResponse = BaseResponse.failure(ResponseMeta.FAILURE, e);
            // 예: return errorResponse;
        }

        return baseResponse;
    }

    public String getChatplayId(String email) {

        String url = API_URL + "/user/info";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Vendor-ID", API_KEY);
        headers.add("Content-Type", "application/json"); // 필요에 따라 추가

//        ChatplayUserInfoReq user = new ChatplayUserInfoReq(email, password);
        ChatplayReq user = new ChatplayReq();
        user.setEmail(email);
        HttpEntity<ChatplayReq> requestEntity = new HttpEntity<>(user, headers);

        String chatplayId = "";

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
//                BaseResponse<ChatplayUserInfoRes> baseResponse = objectMapper.readValue(response.getBody(), new TypeReference<BaseResponse<ChatplayUserInfoRes>>() {});
//                log.info("Chatplay registUser response - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
//                chatplayId = baseResponse.getData().getUserId();
                LinkedHashMap<String,Object> data = new LinkedHashMap<String,Object>();
                BaseResponse<LinkedHashMap> baseResponse = ObjectMapperUtil.readValue(response.getBody(), BaseResponse.class);
                log.info("Chatplay registUser response - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                chatplayId = (String) baseResponse.getData().get("user_id");

            } catch (Exception e) {
                // JSON 파싱 실패 시 (즉, 응답이 BaseResponse 형식이 아닐 때)
                log.warn("Unexpected response format. Raw response: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("Error occurred while registering user: ", e);
        }
        return chatplayId;
    }
}
