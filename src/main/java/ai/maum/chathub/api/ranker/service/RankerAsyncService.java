package ai.maum.chathub.api.ranker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankerAsyncService {

    @Value("${service.ranker-api.schema}")
    private String RANKER_SCHEMA;
    @Value("${service.ranker-api.ip}")
    private String RANKER_IP;
    @Value("${service.ranker-api.port}")
    private String RANKER_PORT;

    private final RestTemplate restTemplate;

    @Async
    public CompletableFuture<Void> callEvaludateApi(String filePath, String param) {

        log.debug("start of callEvaludateApi(async):" + filePath + ":" + param);

        String fastApiUrl = "http://localhost:8000/evaluate";

        Map<String, String> request = new HashMap<>();
        request.put("file_path", filePath);
        request.put("param", param);

        try {
            restTemplate.postForEntity(fastApiUrl, request, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.debug("end of callEvaludateApi(async):" + filePath + ":" + param);

        return CompletableFuture.completedFuture(null);
    }
}
