package ai.maum.chathub.api.chat.dto.elastic;

import ai.maum.chathub.api.engine.dto.EngineParam;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ElasticConfig {
    private Integer topK;
    private Integer knnK;
    private Integer numCandidates;
    private Integer rrfRankConstant;
    private Float rrfSparseWeight;
    private Float rrfDenseWeight;
    private Boolean useVectorReranker;

    public ElasticConfig(List<EngineParam> elasticParam) {
        if(elasticParam != null && elasticParam.size() > 0) {
            for(EngineParam param : elasticParam) {
                String stringValue = param.getValue();
                Double value = Double.valueOf(stringValue==null||stringValue.isBlank()?"0":stringValue);
                switch(param.getKey()) {
                    case("top_k"):
                        this.topK = value.intValue();
                        break;
                    case("knn_k"):
                        this.knnK = value.intValue();
                        break;
                    case("num_candidates"):
                        this.numCandidates = value.intValue();
                        break;
                    case("rrf_rank_constant"):
                        this.rrfRankConstant = value.intValue();
                        break;
                    case("rrf_sparse_weight"):
                        this.rrfSparseWeight = value.floatValue();
                        break;
                    case("rrf_dense_weight"):
                        this.rrfDenseWeight = value.floatValue();
                        break;
                    case("use_vector_reranker"):
                        this.useVectorReranker = (value == null || value.intValue()==1)?true:false;
                        break;
                }
            }
        }
    }

    public Boolean getUseVectorReranker() {
        if(this.useVectorReranker == null)  //값이 NULL 이면 기본 값은 TRUE
            this.useVectorReranker = true;
        return useVectorReranker;
    }
}
