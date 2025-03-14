package ai.maum.chathub.api.chatbotInfo.service;

import ai.maum.chathub.mybatis.mapper.ChatbotInfoMapper;
import ai.maum.chathub.mybatis.vo.ChatbotInfoEmbeddingStats;
import ai.maum.chathub.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {
    @Value("${service.embedding-api}")
    String EMBEDDING_URL;

    private final RestTemplate restTemplate;
    private final ChatbotInfoMapper chatbotInfoMapper;

    @Async
    public void callEmbedding(Long chatbotId, Long functionId,
                              String modelVendor, String modelName, String esIndex, String esUrl,
                              List<ChatbotInfoEmbeddingStats> statusMappingList) {

        Boolean bSuccess = false;

        String apiUrl = EMBEDDING_URL + "/generate_embedding";

        Long seq = null;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chatbot_id", String.valueOf(chatbotId));
        requestBody.put("folder_name", String.valueOf(functionId));
        requestBody.put("vendor", modelVendor);
        requestBody.put("model", modelName);
        requestBody.put("index", esIndex);
        requestBody.put("url", esUrl);
        requestBody.put("seq", seq = System.currentTimeMillis());

        // data = request.json
        // chatbot_id = data.get('chatbot_id')
        // folder_name = data.get('folder_name')
        // model = data.get('model')
        // es_index = data.get('index')
        // es_url = data.get('url')

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.info("EMBEDDING CALL START: {} : {} : {}" , apiUrl, seq, ObjectMapperUtil.writeValueAsString(requestBody));

        String strBody = "";

        try {
            // GET 요청 보내기
            ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            log.info("EMBEDDING CALL RESULT CODE:" + responseEntity.getStatusCode() + ":" + responseEntity.getStatusCodeValue());
            strBody = responseEntity.getBody();
            log.info("EMBEDDING CALL RESULT CODE:" + responseEntity.getStatusCode() + ":" + responseEntity.getStatusCodeValue() + ":" + strBody.toString());
            if(responseEntity.getStatusCode() == HttpStatus.OK)
                bSuccess = true;
        } catch (Exception e) {
            log.info("EMBEDDING CALL FAIL: " + e.getMessage());
            chatbotInfoMapper.updateChatbotInfoEmbeddingStatus(chatbotId, "E");
            Map<String,Object> param = new HashMap<String,Object>();
            param.put("embeddingStatus", "E");
            param.put("list", statusMappingList);
            chatbotInfoMapper.updateChatbotEmbeddingStatus(param);
        }
    }
}
