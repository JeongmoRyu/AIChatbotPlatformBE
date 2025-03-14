package ai.maum.chathub.api.skins.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallSkinsService {
//    @Value("${service.skins.url}")
    private String skinsUrl;
//    @Value("${service.skins.id}")
    private String skinsVendorId;
//    @Value("${service.skins.key}")
    private String skinsApiKey;
    private final RestTemplate restTemplate;

//    public CallSkinsService(RestTemplateBuilder restTemplateBuilder) {
//        this.restTemplate = restTemplateBuilder.build();
//    }

    //    public String callSkinsGet(String uri, Map<String,String> addHeaders) {
    public String callSkins(String uri, HttpMethod type) {

        String apiUrl = skinsUrl + uri;

        if(uri != null && uri.startsWith("http")) {
            apiUrl = uri;
        }

//        log.debug("Headers:{}", ObjectMapperUtil.writeValueAsString(addHeaders));

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Key", skinsApiKey);
        headers.set("Vendor-Id", skinsVendorId);
//        if(addHeaders != null)
//            for(Map.Entry<String,String> entry : addHeaders.entrySet()) {
//                headers.set(entry.getKey(), entry.getValue());
//            }

        HttpEntity<String> entity = new HttpEntity<>(headers);

        log.info("SKINS CALL START:" + skinsApiKey + ":" + skinsVendorId + ":" + apiUrl);

        String strBody = "";

        try {
            // GET 요청 보내기
//            ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
            ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, type, entity, String.class);
            log.info("SKINS CALL RESULT CODE:" + responseEntity.getStatusCode() + ":" + responseEntity.getStatusCodeValue());
            strBody = responseEntity.getBody();
        } catch (Exception e) {
            log.info("SKINS CALL FAIL: " + e.getMessage());
        }

        return strBody;
    }
}
