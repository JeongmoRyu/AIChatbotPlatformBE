package ai.maum.chathub.api.ranker.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//@Data
//@Builder
@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
public class RankerHistoryDetailRes {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Long id;
    private String name;
    private Boolean useFixedChunk;
    private Integer fixedChunkSize;
    private Integer fixedChunkOverlap;
    private Boolean useSemanticChunk;
    private String semanticChunkBpType;
    private String semanticChunkEmbedding;
    private Integer topK;
    private List<String> filePath;;
    private List<JSONObject> embeddingModels;
    @JsonIgnore
    private String strFilePath;
    @JsonIgnore
    private String strEmbeddingModels;
    /*
    String embeddingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
     */
    public List<Object> getEmbeddingModels() {
        List<Object> rtn = new ArrayList<>();
        try {
            rtn = objectMapper.readValue(this.strEmbeddingModels, new TypeReference<List<Object>>() {});
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return rtn;
    }

    public List<String> getfilePath() {
        List<String> rtn = new ArrayList<>();
        try {
            rtn = objectMapper.readValue(this.strFilePath, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return rtn;
    }
}
