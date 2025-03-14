package ai.maum.chathub.mybatis.vo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ai.maum.chathub.conf.document.JsonArrayToStringDeserializer;
import lombok.Getter;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
public class ChatbotInfoVO {
    private Long id;
    private String name;
    private String description;
    private Boolean publicUseYn;
    private Boolean hiddenYn;
    private NormalConversation normalConversation;
    private LLMCommon llmCommon;
    private ReproduceQuestion reproduceQuestion;
//    private FunctionCall functionCall;
//    private ElasticSearch elasticSearch;
    private Rag rag;
//    private UserPrompt userPrompt;
    private String imgFileId;
    @Getter
    @Setter
    public static class NormalConversation {
        private int llmEngineId;
        private int fallbackEngineId;
        private String userPrompt;
        private String systemPrompt;

        @JsonDeserialize(using = JsonArrayToStringDeserializer.class)
        private String parameters;
        private int retry;

        private Boolean useYn;
    }
    @Getter
    @Setter
    public static class LLMCommon {
        private int windowSize;
        private String memoryType;
    }
    @Getter
    @Setter
    public static class ReproduceQuestion {
        private int llmEngineId;
        private int fallbackEngineId;
        private String userPrompt;
        private String systemPrompt;

        @JsonDeserialize(using = JsonArrayToStringDeserializer.class)
        private String parameters;
        private int retry;

        private Boolean useYn;
    }
    @Getter
    @Setter
    public static class FunctionCall {
        private int llmEngineId;
        private List<Function> functions;
        private int fallbackEngineId;
        private String userPrompt;
        private String systemPrompt;

        @JsonDeserialize(using = JsonArrayToStringDeserializer.class)
        private String parameters;
        private int retry;

        private Boolean useYn;
    }
    @Getter
    @Setter
    public static class Function {
        private String filterPrefix;
        private String name;
        private String description;

        @JsonDeserialize(using = JsonArrayToStringDeserializer.class)
        private String preInfoType;

    }
    @Getter
    @Setter
    public static class ElasticSearch {
        private int endpoint;
        private int topK;

        @JsonDeserialize(using = JsonArrayToStringDeserializer.class)
        private String parameters;
        private int retry;

    }
    @Getter
    @Setter
    public static class Rag {
        private int llmEngineId;
        private int fallbackEngineId;
        private int embeddingEngineId;
        private String userPrompt;
        private String systemPrompt;
//        private List<Function> functions;
        private List<Long> functions;
        private ElasticSearch elasticSearch;
        private List<EmbeddingType> embeddingType;
        @JsonDeserialize(using = JsonArrayToStringDeserializer.class)
        private String parameters;
        private int retry;
//        private String embeddingTypeJson;

        private Integer functionLlmEngineId;
        private Integer functionFallbackEngineId;
        private Integer functionRetry;

        private Boolean useYn;

        public String getEmbeddingTypeJson() {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(this.getEmbeddingType());
            } catch (Exception e) {
                return null;
            }
        }

        public String getFunctionsString() {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                if(this.functions != null)
                    return objectMapper.writeValueAsString(this.functions);
                else
                    return "[]";
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    @Getter
    @Setter
    public static class UserPrompt {
        private String consultInfo;
        private String memberInfo;
        private String measureInfo;
        private String geneInfo;

    }
    @Getter
    @Setter
    public static class Parameter {
        private Range range;
        private String label;
        private boolean mandatory;
        private String value;
        private String key;

    }
    @Getter
    @Setter
    public static class Range {
        private String from;
        private String to;

    }

    @Getter
    @Setter
    public static class EmbeddingType {
        private String id;
        private Integer value;;
    }
}
