package ai.maum.chathub.api.agentchat.dto.maumai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaumAIRagResponse {
    List<MaumAIRagKnowlegeItem> knowledgeItems;
    String log;
    String targetLog;
    String promptText;
}
