package ai.maum.chathub.api.chat.dto.openai;

import ai.maum.chathub.api.engine.dto.EngineParam;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAIConfig {
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private Double topP;
    private Double topK;
    private Double frequencyPenalty;
    private Double presencePenalty;
    private boolean stream;

    public OpenAIConfig(List<EngineParam> llmParam) {
        if(llmParam != null && llmParam.size() > 0) {
            for (EngineParam param : llmParam) {
                Double value = Double.valueOf(param.getValue());
                switch (param.getKey()) {
                    case ("top_p"):
                        this.topP = value;
                        break;
                    case ("top_k"):
                        this.topK = value;
                        break;
                    case ("temp"):
                        this.temperature = value;
                        break;
                    case ("pres_p"):
                        this.presencePenalty = value;
                        break;
                    case ("freq_p"):
                        this.frequencyPenalty = value;
                        break;
                    case ("max_token"):
                        this.maxTokens = value.intValue();
                        break;
                }
            }
        }
    }

    public OpenAIConfig() {
    }
}
