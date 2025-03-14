package ai.maum.chathub.api.ranker.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RankerEvaludateReq {

    @Schema(description = "List of embedding models")
    private List<Object> embeddingModels;

    @Schema(description = "Chunking settings")
    private ChunkingSettings chunkingSettings;

    @Schema(description = "Top K value", example = "5")
    private Integer topK;

    @Schema(description = "Name of the evaluation", example = "테테테스트")
    private String name;

    @Schema(description = "Client ID", example = "3a098711-7a0d-4b85-991a-84c23a3983f5")
    private String clientId;

    // Getters and Setters

//    public static class EmbeddingModel {
//
//        @Schema(description = "Name of the model or ensemble model name", example = "BM25")
//        private String name;
//
//        @Schema(description = "Ensemble models list")
//        private List<EnsembleModel> ensemble;
//
//        // Getters and Setters
//    }

//    public static class EnsembleModel {
//
//        @Schema(description = "Model name", example = "snunlp/KR-SBERT-V40K-klueNLI-augSTS")
//        private String model;
//
//        @Schema(description = "Model weight", example = "0.4")
//        private Double weight;
//
//        // Getters and Setters
//    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkingSettings {

        @Schema(description = "Use semantic chunking", example = "true")
        private Boolean useSemanticChunk;

        @Schema(description = "Use fixed chunking", example = "true")
        private Boolean useFixedChunk;

        @Schema(description = "Fixed chunk size", example = "1024")
        private Integer fixedChunkSize;

        @Schema(description = "Fixed chunk overlap size", example = "300")
        private Integer fixedChunkOverlap;

        @Schema(description = "Semantic chunk BP type", example = "percentile")
        private String semanticChunkBpType;

        @Schema(description = "Semantic chunk embedding", example = "text-embedding-ada-002")
        private String semanticChunkEmbedding;

        // Getters and Setters
    }

//    @Schema(description = "엠베딩 모델 리스트", example = "[\n" +
//            "    \"BM25\",\n" +
//            "    {\n" +
//            "      \"name\": \"mymodel1\",\n" +
//            "      \"ensemble\": [\n" +
//            "        {\n" +
//            "          \"model\": \"snunlp/KR-SBERT-V40K-klueNLI-augSTS\",\n" +
//            "          \"weight\": 0.4\n" +
//            "        },\n" +
//            "        {\n" +
//            "          \"model\": \"bongsoo/moco-sentencedistilbertV2.1\",\n" +
//            "          \"weight\": 0.3\n" +
//            "        },\n" +
//            "        {\n" +
//            "          \"model\": \"bongsoo/kpf-sbert-128d-v1\",\n" +
//            "          \"weight\": 0.3\n" +
//            "        }\n" +
//            "      ]\n" +
//            "    },\n" +
//            "    {\n" +
//            "      \"name\": \"mymodel2\",\n" +
//            "      \"ensemble\": [\n" +
//            "        {\n" +
//            "          \"model\": \"sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2\",\n" +
//            "          \"weight\": 0.6\n" +
//            "        },\n" +
//            "        {\n" +
//            "          \"model\": \"smartmind/roberta-ko-small-tsdae\",\n" +
//            "          \"weight\": 0.4\n" +
//            "        }\n" +
//            "      ]\n" +
//            "    }\n" +
//            "  ]")
//    private List<Object> embeddingModels;
//    private ChunkingSettings chunkingSettings;
//    private Integer topK;
//    private String name;
//    private String clientId;
//
//    @Getter
//    @Setter
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class ChunkingSettings {
//        private Boolean useSemanticChunk;
//        private Boolean useFixedChunk;
//        private Integer fixedChunkSize;
//        private Integer fixedChunkOverlap;
//        private String semanticChunkBpType;
//        private String semanticChunkEmbedding;
//    }
}
