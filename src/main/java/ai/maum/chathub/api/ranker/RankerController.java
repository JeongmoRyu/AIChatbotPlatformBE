package ai.maum.chathub.api.ranker;

import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.file.dto.res.SourceFileSaveRes;
import ai.maum.chathub.api.file.service.SourceFileService;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.ranker.dto.req.RankerEvaludateReq;
import ai.maum.chathub.api.ranker.dto.res.RankerHistoryDetailRes;
import ai.maum.chathub.api.ranker.dto.res.RankerHistoryListRes;
import ai.maum.chathub.api.ranker.dto.res.RankerQaRes;
import ai.maum.chathub.api.ranker.dto.res.RankerRankingRes;
import ai.maum.chathub.api.ranker.entity.RankerHistoryEntity;
import ai.maum.chathub.api.ranker.entity.RankerQaEntity;
import ai.maum.chathub.api.ranker.entity.RankerRankingEntity;
import ai.maum.chathub.api.ranker.service.RankerAsyncService;
import ai.maum.chathub.api.ranker.service.RankerService;
import ai.maum.chathub.meta.ResponseMeta;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ranker/")
@RequiredArgsConstructor
@Tag(name="랭커", description="랭커API")
public class RankerController {

    @Value("${service.ranker-api.schema}")
    private String RANKER_SCHEMA;
    @Value("${service.ranker-api.ip}")
    private String RANKER_IP;
    @Value("${service.ranker-api.port}")
    private String RANKER_PORT;

    private final RestTemplate restTemplate;
    private final RankerAsyncService rankerAsyncService;
    private final RankerService rankerService;
    private final SourceFileService sourceFileService;

    @Operation(summary = "QA쌍 조회", description = "특정 랭커 히스토리의 QA를 조회 한다.")
    @GetMapping("/evaluate-history/qa/{ranker_id}")
    public BaseResponse<Page<RankerQaRes>> getRankerQaList(
              @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
            , @Parameter(description = "랭커히스토리ID") @PathVariable(name = "ranker_id", required = false) Long rankerId
            , @Parameter(description = "조회할 page") @RequestParam(defaultValue = "0") int page
            , @Parameter(description = "size/page") @RequestParam(defaultValue = "10") int size
    ) {
        Page<RankerQaRes> result = rankerService.getRankerQaList(rankerId, page, size);
        return BaseResponse.success(result);
    }

    @Operation(summary = "랭킹 조회", description = "특정 랭커 히스토리의 랭킹을 조회 한다.")
    @GetMapping("/evaluate-history/ranking/{ranker_id}")
    public BaseResponse<Page<RankerRankingRes>> getRankerRankingList(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
            , @Parameter(description = "랭커히스토리ID") @PathVariable(name = "ranker_id", required = false) Long rankerId
            , @Parameter(description = "조회할 page") @RequestParam(defaultValue = "0") int page
            , @Parameter(description = "size/page") @RequestParam(defaultValue = "10") int size
    ) {
        Page<RankerRankingRes> result = rankerService.getRankerRankingList(rankerId, page, size);
        return BaseResponse.success(result);
    }

    @Operation(summary = "랭킹 히스토리 목록 조회", description = "랭킹 히스토리 목록 조회")
    @GetMapping("/evaluate-history")
//    public BaseResponse<List<RankerHistoryListRes>> getRankerHistory(
    public BaseResponse<Page<RankerHistoryListRes>> getRankerHistory(
              @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
            , @Parameter(description = "조회할 page") @RequestParam(defaultValue = "0") int page
            , @Parameter(description = "size/page") @RequestParam(defaultValue = "10") int size
            , @Parameter(description = "정렬대상컬럼") @RequestParam(value = "sort_field", defaultValue = "id") String sortField
            , @Parameter(description = "정렬대상기준") @RequestParam(value = "sort_direction", defaultValue = "DESC") String sortDirection
            , @Parameter(description = "내결과만보기") @RequestParam(value = "is_mine", defaultValue = "false") Boolean bIsMine
    ) {
        Long searchUserKey = null;

        if(bIsMine)
            searchUserKey = user.getUserKey();

        Page<RankerHistoryListRes> result = rankerService.getRankerHistoryList(user.getUserKey(), searchUserKey, page, size, sortField, sortDirection);
//        return BaseResponse.success(result.getContent());
        return BaseResponse.success(result);
    }

    @Operation(summary = "랭킹 히스토리 내용 조회", description = "특정 랭킹 히스토리의 상세 내용")
    @GetMapping("/evaluate-history/{ranker_id}")
//    public BaseResponse<List<RankerHistoryListRes>> getRankerHistory(
    public BaseResponse<RankerHistoryDetailRes> getRankerHistoryDetail(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
          , @Parameter(description = "랭커히스토리ID") @PathVariable(name = "ranker_id", required = false) Long rankerId
    ) {
        RankerHistoryDetailRes result = rankerService.getRankerHistoryDetail(rankerId);
        return BaseResponse.success(result);
    }

    @Operation(summary = "랭킹 히스토리 삭제", description = "특정 랭킹 히스토리 삭제")
    @DeleteMapping("/evaluate-history/{ranker_id}")
    public BaseResponse<Void> deleteRankerHistory(
              @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
            , @Parameter(description = "랭커히스토리ID") @PathVariable(name = "ranker_id", required = false) Long rankerId
    ) {
        try {
            rankerService.deleteRankerHistory(rankerId);
            return BaseResponse.success();
        } catch (Exception e) {
            return BaseResponse.failure(e);
        }
    }

    @Operation(summary = "랭킹 측정 실행", description = "랭킹 측정 실행")
    @PostMapping("/evaluate-embeddings")
    public BaseResponse<Map<String, Long>> evaluate(HttpServletRequest request,
              @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
            , @Parameter(description = "옵션값(json)",
                         content = @Content(
                                 mediaType = "application/json",
                                 examples = @ExampleObject(
                                         "{\n" +
                                                 "    \"embedding_models\": [\n" +
                                                 "      \"BM25\",\n" +
                                                 "      {\n" +
                                                 "        \"name\": \"mymodel1\",\n" +
                                                 "        \"ensemble\": [\n" +
                                                 "          {\n" +
                                                 "            \"model\": \"snunlp/KR-SBERT-V40K-klueNLI-augSTS\",\n" +
                                                 "            \"weight\": 0.4\n" +
                                                 "          },\n" +
                                                 "          {\n" +
                                                 "            \"model\": \"bongsoo/moco-sentencedistilbertV2.1\",\n" +
                                                 "            \"weight\": 0.3\n" +
                                                 "          },\n" +
                                                 "          {\n" +
                                                 "            \"model\": \"bongsoo/kpf-sbert-128d-v1\",\n" +
                                                 "            \"weight\": 0.3\n" +
                                                 "          }\n" +
                                                 "        ]\n" +
                                                 "      },\n" +
                                                 "      {\n" +
                                                 "        \"name\": \"mymodel2\",\n" +
                                                 "        \"ensemble\": [\n" +
                                                 "          {\n" +
                                                 "            \"model\": \"sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2\",\n" +
                                                 "            \"weight\": 0.6\n" +
                                                 "          },\n" +
                                                 "          {\n" +
                                                 "            \"model\": \"smartmind/roberta-ko-small-tsdae\",\n" +
                                                 "            \"weight\": 0.4\n" +
                                                 "          }\n" +
                                                 "        ]\n" +
                                                 "      }\n" +
                                                 "    ],\n" +
                                                 "    \"chunking_settings\": {\n" +
                                                 "      \"use_semantic_chunk\": true,\n" +
                                                 "      \"use_fixed_chunk\": true,\n" +
                                                 "      \"fixed_chunk_size\": 1024,\n" +
                                                 "      \"fixed_chunk_overlap\": 300,\n" +
                                                 "      \"semantic_chunk_bp_type\": \"percentile\",\n" +
                                                 "      \"semantic_chunk_embedding\": \"text-embedding-ada-002\"\n" +
                                                 "    },\n" +
                                                 "    \"top_k\": 5,\n" +
                                                 "    \"name\": \"테테테스트\",\n" +
                                                 "    \"client_id\": \"3a098711-7a0d-4b85-991a-84c23a3983f5\"\n" +
                                                 "  }"
                                         )
                         )
                        ) @RequestPart("jsonData") String jsonData
            , @Parameter(description = "첨부파일(multi-part") @RequestPart("files") List<MultipartFile> files
    ) {

        try {

            if(files == null || files.isEmpty() || files.size() < 1)
                return BaseResponse.failure(null, ResponseMeta.PARAM_WRONG);

            // JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonMap = objectMapper.readValue(jsonData, Map.class);

//            RankerEvaludateReq test = objectMapper.readValue(jsonData, RankerEvaludateReq.class);
            List<String> filePaths = new ArrayList<>();
            List<SourceFileSaveRes> fileList = sourceFileService.save(user, files, "DATA");

            if(fileList == null || fileList.size() < 1) {
                return BaseResponse.failure(null, ResponseMeta.FILE_UPLOAD_ERROR);
            }

            for(SourceFileSaveRes sourceFile:fileList) {
                filePaths.add(sourceFile.getPath());
            }

            jsonMap.put("file_paths", filePaths);

            // FastAPI URL 및 헤더 구성
            String fastApiUrl = rankerService.buildFastApiUrl(request);
            HttpHeaders headers = rankerService.buildHeaders(request);

            log.debug("url:" + fastApiUrl);
            log.debug("header:" + headers.toString());

            RankerHistoryEntity rankerHistory = rankerService.saveRankerHistory(jsonMap, user.getUserKey(), fileList);

            if(rankerHistory == null || rankerHistory.getId() < 1L || "E".equals(rankerHistory.getEmbeddingStatus())) {
                //저장실패
                return BaseResponse.failure(null, ResponseMeta.FAILURE);
            }

            jsonMap.put("history_id", rankerHistory.getId());
            jsonMap.put("user_key", user.getUserKey());

            // 엔터티 생성
//            HttpEntity<?> entity = rankerService.buildRequestEntity(jsonMap, headers);
            HttpEntity<?> entity = rankerService.buildRequestEntity(jsonMap);

            // FastAPI에 동기적으로 호출 후 결과 확인
            boolean fastApiSuccess = rankerService.callEvaludateApi(fastApiUrl, entity);

            // FastAPI 호출 성공 여부에 따라 응답 반환
            if (fastApiSuccess) {
                Map<String, Long> resultMap = new HashMap<String, Long>();
                resultMap.put("history_id", rankerHistory.getId());
                return BaseResponse.success(resultMap, ResponseMeta.SUCCESS);
            } else {
                return BaseResponse.failure(null, ResponseMeta.RANKER_API_CALL_ERROR);
            }
        } catch (Exception e) {
            log.error("evaluate:fastapi call error:{}", e.getMessage());
            return BaseResponse.failure(null, ResponseMeta.FAILURE);
        }
    }

    // 공통 GET 메서드
    @Operation(summary = "리다이렉트용 (미사용)", description = "리다이렉트용 (미사용)")
    @GetMapping("**")
    public ResponseEntity<String> proxyGet(HttpServletRequest request) {
        String fastApiUrl = rankerService.buildFastApiUrl(request);
        HttpHeaders headers = rankerService.buildHeaders(request);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(fastApiUrl, HttpMethod.GET, entity, String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    // 공통 POST 메서드
    @Operation(summary = "리다이렉트용 (미사용)", description = "리다이렉트용 (미사용)")
    @PostMapping("**")
    public ResponseEntity<String> proxyPost(HttpServletRequest request, @RequestBody String body) {
        String fastApiUrl = rankerService.buildFastApiUrl(request);
        HttpHeaders headers = rankerService.buildHeaders(request);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(fastApiUrl, HttpMethod.POST, entity, String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
