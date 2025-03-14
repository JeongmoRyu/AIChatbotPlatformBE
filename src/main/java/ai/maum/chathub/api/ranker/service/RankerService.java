package ai.maum.chathub.api.ranker.service;

import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.api.file.dto.res.SourceFileSaveRes;
import ai.maum.chathub.api.ranker.dto.RankerModelDto;
import ai.maum.chathub.api.ranker.dto.res.RankerHistoryDetailRes;
import ai.maum.chathub.api.ranker.dto.res.RankerHistoryListRes;
import ai.maum.chathub.api.ranker.dto.res.RankerQaRes;
import ai.maum.chathub.api.ranker.dto.res.RankerRankingRes;
import ai.maum.chathub.api.ranker.entity.*;
import ai.maum.chathub.api.ranker.repo.*;
import ai.maum.chathub.meta.ResponseMeta;
import ai.maum.chathub.mybatis.mapper.RankerMapper;
import ai.maum.chathub.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankerService {

    @Value("${service.ranker-api.schema}")
    private String RANKER_SCHEMA;
    @Value("${service.ranker-api.ip}")
    private String RANKER_IP;
    @Value("${service.ranker-api.port}")
    private String RANKER_PORT;

    private final RestTemplate restTemplate = new RestTemplate();
    private final RankerHistoryRepository rankerHistoryRepository;
    private final RankerFileRepository rankerFileRepository;
    private final RankerModelEnsembleRepository rankerModelEnsembleRepository;
    private final RankerModelRepository rankerModelRepository;
    private final RankerMapper rankerMapper;
    private final RankerQaRepository rankerQaRepository;
    private final RankerRankingRepository rankerRankingRepository;

    public Page<RankerQaRes> getRankerQaList(Long rankerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<RankerQaEntity> rankerQaEntities = rankerQaRepository.findByRankerHistoryEntity_Id(rankerId, pageable);
//        return rankerQaRepository.findByRankerHistoryEntity_Id(rankerId, pageable);
        // ROW_NUMBER를 계산하며 DTO로 매핑
        List<RankerQaRes> content = rankerQaEntities.getContent().stream()
                .map(entity -> new RankerQaRes(
                        pageable.getOffset() + rankerQaEntities.getContent().indexOf(entity) + 1, // Row Number 계산
                        entity.getId(),
                        entity.getQuestion(),
                        entity.getAnswer(),
                        entity.getDocId(),
                        entity.getChunk()
                ))
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, rankerQaEntities.getTotalElements());
    }

    public Page<RankerRankingRes> getRankerRankingList(Long rankerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "hitAccuracy");
        Page<RankerRankingEntity> rankerRankingEntities = rankerRankingRepository.findByRankerHistoryEntity_Id(rankerId, pageable);

        List<RankerRankingRes> content = rankerRankingEntities.getContent().stream()
                .map(entity -> new RankerRankingRes(
                        pageable.getOffset() + rankerRankingEntities.getContent().indexOf(entity) + 1, // Row Number 계산
                        entity.getId(),
                        entity.getModelName(),
                        entity.getEmbeddingModelConfig(),
                        entity.getHitAccuracy(),
                        entity.getDescription()
                ))
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, rankerRankingEntities.getTotalElements());

//        return rankerRankingRepository.findByRankerHistoryEntity_Id(rankerId, pageable);
    }

    public void deleteRankerHistory(Long id) {
        rankerHistoryRepository.deleteById(id);
    }

    public Page<RankerHistoryListRes>  getRankerHistoryList(Long myUserKey, Long searchUserKey, int page, int size, String sortField, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
//        return rankerHistoryRepository.findAllWithUserKey(myUserKey, searchUserKey, pageable);
        return getRankerHistoriesWithRowNumber(myUserKey, searchUserKey, pageable);
    }

    public Page<RankerHistoryListRes> getRankerHistoriesWithRowNumber(
            Long myUserKey,
            Long searchUserKey,
            Pageable pageable
    ) {
        Page<RankerHistoryListRes> results = rankerHistoryRepository.findAllWithUserKey(myUserKey, searchUserKey, pageable);

        // rowNumber 계산
        AtomicLong rowNumberCounter = new AtomicLong(pageable.getOffset() + 1);

        List<RankerHistoryListRes> contentWithRowNumbers = results.getContent().stream()
                .map(res -> {
                    res.setRowNumber(rowNumberCounter.getAndIncrement());
                    return res;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(contentWithRowNumbers, pageable, results.getTotalElements());
    }

    public RankerHistoryDetailRes getRankerHistoryDetail(Long rankerId) {

        RankerHistoryEntity rankerHistory = rankerHistoryRepository.findById(rankerId).orElseThrow(
                () -> BaseException.of(ResponseMeta.NO_DATA)
        );

        List<RankerFileEntity> rankerFileList = rankerFileRepository.findByRankerHistoryEntity_Id(rankerId);
        List<String> modelList = rankerMapper.selectModelDetailList(rankerId);

        RankerHistoryDetailRes result = rankerMapper.selectModelDetail(rankerId);

        return result;
    }


    @Transactional
    public RankerHistoryEntity saveRankerHistory(Map<String, Object> jsonMap, Long userKey, List<SourceFileSaveRes> fileList) {

        Long rankerHistoryId = 0L;
        RankerHistoryEntity rankerHistory = new RankerHistoryEntity();
        List<RankerFileEntity> rankerFiles = new ArrayList<>();
//        List<RankerModelEntity> rankerModels = new ArrayList<>();
//        List<RankerModelEnsembleEntity> rankerModelEnsembles = new ArrayList<>();

        try {

            Map<String,Object> chunkingSettings = (Map<String,Object>) jsonMap.get("chunking_settings");
            List<Object> embeddingModels = (List<Object>) jsonMap.get("embedding_models");

            //RankerHistory insert
            rankerHistory.setName((String)jsonMap.get("name")==null?"no_name":(String)jsonMap.get("name"));
            rankerHistory.setUserKey(userKey);
            rankerHistory.setTopK((int)jsonMap.get("top_k"));
            rankerHistory.setUseSemanticChunk((Boolean)chunkingSettings.get("use_semantic_chunk")?"Y":"N");
            rankerHistory.setUseFixedChunk((Boolean)chunkingSettings.get("use_fixed_chunk")?"Y":"N");
            rankerHistory.setFixedChunkSize((int)chunkingSettings.get("fixed_chunk_size"));
            rankerHistory.setFixedChunkOverlap((int)chunkingSettings.get("fixed_chunk_overlap"));
            rankerHistory.setSemanticChunkBpType((String)chunkingSettings.get("semantic_chunk_bp_type"));
            rankerHistory.setSemanticChunkEmbedding((String)chunkingSettings.get("semantic_chunk_embedding"));
            rankerHistory.setEmbeddingStatus("P");
            rankerHistory = rankerHistoryRepository.save(rankerHistory);
            log.debug("insert rankerHistory {}", rankerHistory.getId(), rankerHistory.getName(), rankerHistory.getUserKey());

            if(rankerHistory == null || rankerHistory.getId() == null || rankerHistory.getId() < 1) {
                throw new Exception("Rank History insert error");
            }

            rankerHistoryId = rankerHistory.getId();

            //RankerFile inert
            for(SourceFileSaveRes sourceFileSaveRes:fileList) {
                RankerFileEntity rankerFile = new RankerFileEntity(rankerHistory, sourceFileSaveRes.getOrgName(), sourceFileSaveRes.getSize());
                rankerFiles.add(rankerFile);
            }

            rankerFileRepository.saveAll(rankerFiles);

            //RankerModel insert
            for(Object item:embeddingModels) {
                RankerModelEntity rankerModel = new RankerModelEntity();
                rankerModel.setRankerHistoryEntity(rankerHistory);
//                rankerModel.setRankerId(rankerHistoryId);
                if(item instanceof String) {    //일반 모델의 경우
                    rankerModel.setName((String)item);
                    rankerModelRepository.save(rankerModel);
                } else if(item instanceof LinkedHashMap) {    //앙상블 모델의 경우
                    log.debug(item.toString());

                    LinkedHashMap<String,Object> myModel =(LinkedHashMap<String,Object>)item;
                    rankerModel.setName((String) myModel.get("name"));
                    rankerModel = rankerModelRepository.save(rankerModel);

                    Long rankerModelId = rankerModel.getId();
                    List<RankerModelEnsembleEntity> ensembleList = new ArrayList<>();
                    for (Map<String, Object> submodel : (List<Map<String, Object>>) myModel.get("ensemble")) {
                        ensembleList.add(new RankerModelEnsembleEntity(rankerModel,
                                (String) submodel.get("model"),
                                (Double) submodel.get("weight")));
                    }
                    if (ensembleList != null && ensembleList.size() > 0)
                        rankerModelEnsembleRepository.saveAll(ensembleList);
                } else {
                    throw new Exception ("not model exist");
                }
            }
        } catch (Exception e) {
            log.error("parsing error {}, {}", e.getMessage(), jsonMap==null?"": ObjectMapperUtil.writeValueAsString(jsonMap));
            rankerHistory.setEmbeddingStatus("E");
            rankerHistory = rankerHistoryRepository.save(rankerHistory);
        }

        return rankerHistory;

        /*
        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {

            String key = entry.getKey();     // 키를 가져옴
            Object value = entry.getValue(); // 값을 가져옴

            // 키와 값을 처리하는 로직
            log.debug("Key: " + key + ", Value: " + value);

            try {

                switch (key) {
                    case ("file_paths"):
                        List<String> paths = (List<String>) value;
                        for(String item:paths) {
                            RankerFileEntity rankerFile = new RankerFileEntity();
                        }
                        break;
                    case ("embedding_models"):
                        break;
                    case ("chunking_setting"):
                        break;
                    case ("top_k"):
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                log.error("parsing error {}, {}", key, value==null?"": ObjectMapperUtil.writeValueAsString(value));
            }
        }
        */
    }

    private RankerHistoryEntity insertRankHistory(RankerHistoryEntity rankerHistory) {
        log.debug("insertRankHistory");
        return rankerHistoryRepository.save(rankerHistory);
    }

    public boolean callEvaludateApi(String fastApiUrl, HttpEntity<?> entity) {

        boolean bRtn = false;

        log.debug("start of callEvaludateApi(sync):" + entity);

        try {
            ResponseEntity<String> response = restTemplate.exchange(fastApiUrl, HttpMethod.POST, entity, String.class);
//            ResponseEntity<String> response = restTemplate.postForEntity(fastApiUrl, request, String.class);

            // FastAPI가 성공 응답을 준 경우 true 반환
            bRtn = response.getStatusCode() == HttpStatus.OK;

        } catch (Exception e) {
            e.printStackTrace();
            // 네트워크 오류 발생 시 false 반환
        }

        log.debug("end of callEvaludateApi(sync):" + entity);

        return bRtn;
    }

    public HttpEntity<MultiValueMap<String, Object>> buildMultipartRequestEntity(
            List<MultipartFile> files,
            Map<String, Object> jsonMap,
            HttpHeaders headers) throws Exception {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // 파일 추가
        for (MultipartFile file : files) {
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("files", fileResource);
        }

        // JSON 데이터 추가
        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            body.add(entry.getKey(), entry.getValue());
        }

        return new HttpEntity<>(body, headers);
    }

//    public HttpEntity<MultiValueMap<String, Object>> buildRequestEntity(
//            Map<String, Object> jsonMap,
//            HttpHeaders headers) {
//
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//
//        // JSON 데이터 추가 (file_paths 포함)
//        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
//            body.add(entry.getKey(), entry.getValue());
//        }
//
//        return new HttpEntity<>(body, headers);
//    }

    public HttpEntity<Map<String, Object>> buildRequestEntity(
            Map<String, Object> jsonMap) {

        // JSON 데이터를 직접 Map으로 넣음
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        return new HttpEntity<>(jsonMap, headers); // MultiValueMap을 사용하지 않음
    }

//    public HttpEntity<MultiValueMap<String, Object>> buildRequestEntity(
//            Map<String, Object> jsonMap) {
//
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//
//        // JSON 데이터 추가 (file_paths 포함)
//        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
//            body.add(entry.getKey(), entry.getValue());
//        }
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/json");
//
//        return new HttpEntity<>(body, headers);
//    }

    // FastAPI로 보낼 URL을 빌드
    public String buildFastApiUrl(HttpServletRequest request) {
        String originalUri = request.getRequestURI().replace("/ranker", "");
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        return RANKER_SCHEMA + "://" + RANKER_IP + ":" + RANKER_PORT + originalUri + queryString;
//        return RANKER_URL + originalUri + queryString;
//        return "http://127.0.0.1:8000" + originalUri + queryString;
    }

    // 모든 헤더를 복사
    public HttpHeaders buildHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.set(headerName, request.getHeader(headerName));
        }

        return headers;
    }
}
