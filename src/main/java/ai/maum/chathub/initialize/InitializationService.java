package ai.maum.chathub.initialize;

import ai.maum.chathub.mybatis.mapper.EngineMapper;
import ai.maum.chathub.mybatis.vo.ElasticVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitializationService {

    @Value("${service.elastic.username}")
    private String ES_USER;

    @Value("${service.elastic.password}")
    private String ES_PASS;

    private final InitializationStatusRepository initializationStatusRepository;
    private final EngineMapper engineMapper;
    private final RestTemplate restTemplate;

    @Transactional
    public void performInitializationIfNeeded() {
        // DB에서 초기화 상태 확인
        boolean isInitialized = initializationStatusRepository.findById(1L)
                .map(InitializationStatus::isInitialized)
                .orElse(false);

        if (!isInitialized) {
            // 초기화 작업 수행
            initializeApplication();

            // 초기화 상태 업데이트
            InitializationStatus status = initializationStatusRepository.findById(1L)
                    .orElse(new InitializationStatus());
            status.setInitialized(true);
            status.setLastInitializedAt(LocalDateTime.now());
            initializationStatusRepository.save(status);
        }
    }

    private void initializeApplication() {
        // 초기화 작업 수행 예시
        System.out.println("Initializing application...");
        // 예: 데이터베이스 기본 데이터 삽입, 외부 API 호출 등

        ElasticVO elasticInfo = engineMapper.selectElasticEngineInfoById(1L);

        if(elasticInfo == null) {
            log.debug("no elastic data!!!");
            return;
        }

        if(elasticInfo.getApik() == null || elasticInfo.getApik().isEmpty()) {
            log.debug("elastic api key is null");
            generateElasticApiKey(elasticInfo);
        } else {
            log.debug("elastic api key is not null : {}", elasticInfo.getApik());
        }

        generateElasticIndex(elasticInfo);

    }

    private void generateElasticApiKey(ElasticVO elasticInfo) {

        String url = elasticInfo.getUrl() + "/_security/api_key";

        // 요청 JSON
        Map<String, Object> requestBody = Map.of(
                "name", "custom-api-key",
                "role_descriptors", Map.of(
                        "custom-role", Map.of(
                                "cluster", Collections.singletonList("all"),
                                "index", Collections.singletonList(
                                        Map.of(
                                                "names", Collections.singletonList("*"),
                                                "privileges", Collections.singletonList("all")
//                                                    "privileges", Collections.singletonList("read, write")
                                        )
                                )
                        )
                )
        );

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(ES_USER, ES_PASS);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Elasticsearch API 호출
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );

        // API 키 저장
        String apiKey = (String) response.getBody().get("api_key");
        String encoded = (String) response.getBody().get("encoded");

        elasticInfo.setApik(encoded);

        // DB에 저장
        engineMapper.setElasticApiKey(elasticInfo);
    }

    void generateElasticIndex(ElasticVO elasticInfo) {

        List<String> indexList = new ArrayList<>();

//        String url = elasticInfo.getUrl() + "/" + elasticInfo.getIndex1();

        if(elasticInfo.getIndex1() != null && !elasticInfo.getIndex1().isEmpty()) {
            if(!indexExists(elasticInfo.getUrl() + "/" + elasticInfo.getIndex1()))
                indexList.add(elasticInfo.getUrl() + "/" + elasticInfo.getIndex1());
        }

        if(elasticInfo.getIndex2() != null && !elasticInfo.getIndex2().isEmpty())
            if(!indexExists(elasticInfo.getUrl() + "/" + elasticInfo.getIndex2()))
                indexList.add(elasticInfo.getUrl() + "/" + elasticInfo.getIndex2());

        // Body
        String indexMapping = """
                    {
                      "mappings": {
                        "properties": {
                          "metadata": {
                            "properties": {
                              "group": {
                                "type": "text",
                                "fields": {
                                  "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                  }
                                }
                              },
                              "index": {
                                "type": "long"
                              },
                              "page": {
                                "type": "long"
                              },
                              "sheet": {
                                "type": "text",
                                "fields": {
                                  "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                  }
                                }
                              },
                              "source": {
                                "type": "text",
                                "fields": {
                                  "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                  }
                                }
                              },
                              "source_uuid": {
                                "type": "text",
                                "fields": {
                                  "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                  }
                                }
                              },
                              "uuid": {
                                "type": "text",
                                "fields": {
                                  "keyword": {
                                    "type": "keyword",
                                    "ignore_above": 256
                                  }
                                }
                              }
                            }
                          },
                          "text": {
                            "type": "text",
                            "fields": {
                              "keyword": {
                                "type": "keyword",
                                "ignore_above": 256
                              }
                            }
                          },
                          "vector": {
                            "type": "dense_vector",
                            "dims": 1536,
                            "index": true,
                            "similarity": "cosine",
                            "index_options": {
                              "type": "int8_hnsw",
                              "m": 16,
                              "ef_construction": 100
                            }
                          },
                          "vector-ko-sroberta-multitask": {
                            "type": "dense_vector",
                            "dims": 768,
                            "index": true,
                            "similarity": "cosine",
                            "index_options": {
                              "type": "int8_hnsw",
                              "m": 16,
                              "ef_construction": 100
                            }
                          },
                          "vector-multilingual-e5-large-instruct": {
                            "type": "dense_vector",
                            "dims": 1024,
                            "index": true,
                            "similarity": "cosine",
                            "index_options": {
                              "type": "int8_hnsw",
                              "m": 16,
                              "ef_construction": 100
                            }
                          }
                        }
                      }
                    }
                """;

        for(String indexUrl : indexList) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBasicAuth(ES_USER, ES_PASS);

            // Create the HTTP entity
            HttpEntity<String> entity = new HttpEntity<>(indexMapping, headers);

            // Send the PUT request to create the index
            ResponseEntity<String> response = restTemplate.exchange(indexUrl, HttpMethod.PUT, entity, String.class);

            // Handle response
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Index created successfully: " + indexUrl);
            } else {
                System.out.println("Failed to create index: " + response.getBody());
            }
        }
    }

    private boolean indexExists(String indexUrl) {
        try {
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(ES_USER, ES_PASS);

            // Create the HTTP entity
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Send a HEAD request to check if the index exists
            ResponseEntity<String> response = restTemplate.exchange(indexUrl, HttpMethod.HEAD, entity, String.class);

            // If the status code is 200, the index exists
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            // If a 404 error is returned, the index does not exist
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
