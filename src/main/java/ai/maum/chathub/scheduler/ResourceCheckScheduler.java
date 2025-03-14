package ai.maum.chathub.scheduler;

import ai.maum.chathub.api.file.entity.SourceFileEntity;
import ai.maum.chathub.api.file.service.SourceFileService;
import ai.maum.chathub.mybatis.mapper.ResourceCheckMapper;
import ai.maum.chathub.mybatis.vo.ElasticVO;
import ai.maum.chathub.util.DateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceCheckScheduler {

    private final ResourceCheckMapper resourceCheckMapper;
    private final RestTemplate restTemplate;
    private final SourceFileService sourceFileService;

    public void testResourceChecker() {
        executeResourceCheck();
    }

//    @Profile({"dev", "stg", "prd"})
//    @Scheduled(cron = "0 0 1 * * ?")
//    public void executeAtOneAM() {
//        executeResourceCheck();
//    }
//
//    //    @Scheduled(fixedRate = n * 1000 ) // n초마다 실행
//    @Profile("local")
//    @Scheduled(fixedRate = 5 * 1000 )
//    public void executeEveryOneMinute() {
//        executeResourceCheck();
//    }

//    @Scheduled(fixedRate = 1 * 60 * 1000 )
    @Scheduled(cron = "0 0 1 * * ?")
    private void executeResourceCheck() {
        log.debug("execute Resource Check at {}", DateUtil.convertToStringByMs(System.currentTimeMillis()));

        //현재 등록된 챗봇이 사용하는 function 목록
        //현재 등록된 챗봇이 사용하는 펑션이 사용하는 file 목록
        //현재 등록된 챗봇이 사용하는 이미지 목록
        //현재 등록된 펑션이 사용하는 이미지 목록

        //삭제대상 파일 처리
        List<SourceFileEntity> deleteFileList = resourceCheckMapper.selectGabageFiles();
        for(SourceFileEntity item: deleteFileList) {
            Boolean bDelete = sourceFileService.deleteSourceFileIfExist(item.getId());
            log.debug("{}, {}, {}, {}, {}, {}, {}, {}",
                    bDelete, item.getId(), item.getName(), item.getOrgName(), item.getUserName(), item.getSize(), item.getPath(), item.getType());
        }

        //사용중 ES filter_prefix 처리
        List<String> validESFilterPreFixList = resourceCheckMapper.selectValidESFilterPreFix();

        List<ElasticVO> elasticList = resourceCheckMapper.selectElasticEngineList();
        for(ElasticVO item:elasticList) {
            log.debug("elastic:{},{},{},{},{},{}", item.getId(), item.getName(), item.getUrl(), item.getApik(), item.getIndex1(), item.getIndex2());

//            String ELASTICSEARCH_URL = "http://localhost:9200/aabc-mcl-wiki/_search";
            String esUrl = (item.getUrl().endsWith("/") ? item.getUrl() : item.getUrl() + "/");
            List<String> preFixListIndex1 = fetchAllGroupKeywords(esUrl, item.getApik(), item.getIndex1());
            for(String prefix:preFixListIndex1) {
                Boolean bUsing = validESFilterPreFixList.contains(prefix);
                log.debug("{},{},{},{}", bUsing, esUrl, item.getIndex1(), prefix);
                if(!bUsing) {
//                    log.debug("delete... {},{}", esUrl, prefix);
                    deleteByGroupKeyword(esUrl, item.getApik(), item.getIndex1(), prefix);
                }
            }

            List<String> preFixListIndex2 = fetchAllGroupKeywords(esUrl, item.getApik(), item.getIndex2());
            for(String prefix:preFixListIndex2) {
                Boolean bUsing = validESFilterPreFixList.contains(prefix);
                log.debug("{},{},{},{}", bUsing, esUrl, item.getIndex2(), prefix);
                if(!bUsing) {
//                    log.debug("delete... {},{}", esUrl, prefix);
                    deleteByGroupKeyword(esUrl, item.getApik(), item.getIndex2(), prefix);
                }
            }
        }
    }

//    private void getESFilterPreFix(ElasticVO elasticInfo) {
//
//    }


    private List<String> fetchAllGroupKeywords(String esUrl, String apiKey, String index) {
        List<String> allGroups = new ArrayList<>();
        String afterKey = null;

        String esUri = esUrl + index + "/_search";

        do {
            // Elasticsearch 요청 body 생성
            Map<String, Object> requestBody = createRequestBody(afterKey);

            // 요청 보내기
            ResponseEntity<Map> response = sendRequestToElasticsearch(esUri, apiKey, requestBody);

            // 응답에서 group 키워드 추출 및 저장
            List<Map<String, Object>> buckets = getBucketsFromResponse(response);
            for (Map<String, Object> bucket : buckets) {
                Map<String, Object> keyMap = (Map<String, Object>) bucket.get("key");
                allGroups.add((String) keyMap.get("group"));
            }

            // after_key 업데이트
            afterKey = getAfterKeyFromResponse(response);

        } while (afterKey != null);

        return allGroups;
    }

    private Map<String, Object> createRequestBody(String afterKey) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("size", 0);

        // aggregation 설정
        Map<String, Object> aggs = new HashMap<>();
        Map<String, Object> composite = new HashMap<>();
        List<Map<String, Object>> sourcesList = new ArrayList<>();  // 리스트로 변경

        // terms를 명확하게 정의
        Map<String, Object> groupField = new HashMap<>();
        Map<String, Object> terms = new HashMap<>();
        terms.put("field", "metadata.group.keyword");
        groupField.put("terms", terms);

        Map<String, Object> groupMap = new HashMap<>();
        groupMap.put("group", groupField);  // group 필드를 추가

        sourcesList.add(groupMap);  // 리스트에 추가

        composite.put("size", 10);  // 한 번에 가져올 데이터 수 설정
        composite.put("sources", sourcesList);  // 리스트 전달

        if (afterKey != null) {
            Map<String, String> after = new HashMap<>();
            after.put("group", afterKey);
            composite.put("after", after);  // after_key 추가
        }

        aggs.put("group_values", Map.of("composite", composite));
        requestBody.put("aggs", aggs);

        return requestBody;
    }
    private ResponseEntity<Map> sendRequestToElasticsearch(String esUrl, String apiKey, Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "ApiKey " + apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.exchange(esUrl, HttpMethod.POST, entity, Map.class);
    }

    private List<Map<String, Object>> getBucketsFromResponse(ResponseEntity<Map> response) {
        Map<String, Object> body = response.getBody();
        Map<String, Object> aggregations = (Map<String, Object>) body.get("aggregations");
        Map<String, Object> groupValues = (Map<String, Object>) aggregations.get("group_values");
        return (List<Map<String, Object>>) groupValues.get("buckets");
    }

    private String getAfterKeyFromResponse(ResponseEntity<Map> response) {
        Map<String, Object> body = response.getBody();
        Map<String, Object> aggregations = (Map<String, Object>) body.get("aggregations");
        Map<String, Object> groupValues = (Map<String, Object>) aggregations.get("group_values");
        Map<String, Object> afterKeyMap = (Map<String, Object>) groupValues.get("after_key");

        if (afterKeyMap != null) {
            return (String) afterKeyMap.get("group");
        }

        return null;
    }

    public void deleteByGroupKeyword(String esUrl, String apiKey, String index, String keyword) {
        // Elasticsearch Delete by Query API의 URL 설정
//        String esUrl = ELASTICSEARCH_URL + "/" + index + "/_delete_by_query";
        String esUri = esUrl + index + "/_delete_by_query";

        // Delete by Query 요청 body 생성
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> term = new HashMap<>();
        term.put("metadata.group.keyword", keyword);  // keyword 조건 추가
        query.put("term", term);
        requestBody.put("query", query);

        // Elasticsearch에 요청 보내기
        ResponseEntity<Map> response = sendRequestToElasticsearch(esUri, apiKey, requestBody);

        // 응답 처리 (예: 응답 상태 출력)
        if (response.getStatusCode().is2xxSuccessful()) {
            log.debug("성공:{}", response.getStatusCode());
        } else {
            log.debug("실패:{}", response.getStatusCode(), response.getBody());
//            System.err.println("문서 삭제 실패: " + response.getBody());
        }
    }

    public void copyESData() {
        // 1. 데이터 조회
        List<Map<String, Object>> documents = fetchDataByGroupKeyword("http://localhost:9200", "S0RDSzFaRUJtc0RqRlA2MDFpajc6UEh5LURaU0dUcHF3VUx4enZWbGlnQQ==", "aabc-mcl-wiki", "1. 피부고민");
        // 2. 데이터 삽입 (새로운 keyword로)
        try {
            insertDataToNewIndex("http://localhost:9200", "S0RDSzFaRUJtc0RqRlA2MDFpajc6UEh5LURaU0dUcHF3VUx4enZWbGlnQQ==", "aabc-mcl-wiki-dev", documents, "1. 피부고민");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Map<String, Object>> fetchDataByGroupKeyword(String esUrl, String apiKey, String index, String keyword) {
        String esUri = esUrl + "/" + index + "/_search?scroll=1m";
        List<Map<String, Object>> allDocuments = new ArrayList<>();

        // 초기 검색 요청
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("size", 1000);  // 한 번에 1000개씩 가져오기
        requestBody.put("_source", true);  // 전체 _source 필드를 반환

        Map<String, Object> query = new HashMap<>();
        Map<String, Object> term = new HashMap<>();
        term.put("metadata.group.keyword", keyword);
        query.put("term", term);
        requestBody.put("query", query);

        ResponseEntity<Map> response = sendRequestToElasticsearch(esUri, apiKey, requestBody);

        // Scroll ID 및 첫 번째 batch의 문서들 가져오기
        Map<String, Object> responseBody = response.getBody();
        String scrollId = (String) responseBody.get("_scroll_id");
        Map<String, Object> hits = (Map<String, Object>) responseBody.get("hits");
        List<Map<String, Object>> documents = (List<Map<String, Object>>) hits.get("hits");
        allDocuments.addAll(documents);

        // Scroll API를 이용해 반복적으로 추가 데이터를 가져옴
        while (documents.size() > 0) {
            // Scroll 요청 생성
            Map<String, Object> scrollRequestBody = new HashMap<>();
            scrollRequestBody.put("scroll", "1m");
            scrollRequestBody.put("scroll_id", scrollId);

            // 스크롤을 이용한 추가 데이터 요청
            response = sendRequestToElasticsearch(esUrl + "/_search/scroll", apiKey, scrollRequestBody);
            responseBody = response.getBody();

            // 새로운 문서 및 scrollId 가져오기
            scrollId = (String) responseBody.get("_scroll_id");
            hits = (Map<String, Object>) responseBody.get("hits");
            documents = (List<Map<String, Object>>) hits.get("hits");

            // 새로운 문서를 allDocuments에 추가
            allDocuments.addAll(documents);
        }

        // 마지막으로 스크롤을 삭제 (선택)
        deleteScroll(scrollId, esUrl, apiKey);

        return allDocuments;
    }

    // 스크롤 삭제 (옵션)
    private void deleteScroll(String scrollId, String esUrl, String apiKey) {
        String esUri = esUrl + "/_search/scroll";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("scroll_id", scrollId);

        sendRequestToElasticsearch(esUri, apiKey, requestBody);
    }

    // 2. 데이터를 새로운 인덱스에 삽입하는 메서드
    private void insertDataToNewIndex(String esUrl, String apiKey, String newIndex, List<Map<String, Object>> documents, String newKeyword) throws JsonProcessingException {
        String esUri = esUrl + "/" + newIndex + "/_bulk";

        StringBuilder bulkRequestBody = new StringBuilder();
        ObjectMapper objectMapper = new ObjectMapper();  // Jackson ObjectMapper

        // _bulk 요청에 맞게 데이터 구성
        for (Map<String, Object> doc : documents) {
            Map<String, Object> source = (Map<String, Object>) doc.get("_source");

            // metadata.group.keyword 변경
            Map<String, Object> metadata = (Map<String, Object>) source.get("metadata");
            metadata.put("group.keyword", newKeyword);

            // index 명령 추가
            bulkRequestBody.append("{ \"index\": { \"_index\": \"" + newIndex + "\" } }\n");

            // 문서를 JSON으로 직렬화하여 추가
            bulkRequestBody.append(objectMapper.writeValueAsString(source) + "\n");
        }

        // 마지막에도 줄바꿈을 추가
        bulkRequestBody.append("\n");

        // Elasticsearch에 _bulk 요청 보내기
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("body", bulkRequestBody.toString());
//
//        sendRequestToElasticsearch(esUri, apiKey, requestBody);

        // Elasticsearch에 _bulk 요청 보내기
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "ApiKey " + apiKey);

        // Map을 사용하지 않고 직접 raw JSON 문자열로 전송
        HttpEntity<String> entity = new HttpEntity<>(bulkRequestBody.toString(), headers);
        ResponseEntity<Map> response = restTemplate.exchange(esUri, HttpMethod.POST, entity, Map.class);

        // 응답 처리
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("데이터가 성공적으로 삽입되었습니다.");
        } else {
            System.err.println("데이터 삽입 실패: " + response.getBody());
        }
    }
}
