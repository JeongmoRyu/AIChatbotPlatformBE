package ai.maum.chathub.api.chat.dto.maumai;

import ai.maum.chathub.api.chat.dto.openai.OpenAIChatMessage;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MaumAIParam {
    //LLM message
    private List<OpenAIChatMessage> utterances;

    //LLM 범용 config
    private MaumAIConfig config;

    //LLM LlamaBase model config
    private MaumAIConfig generationConfig;

    //RAG message
    private String utterance;
    private Integer numItems;
    private String agentId;
    private List<String> conditions;
}
