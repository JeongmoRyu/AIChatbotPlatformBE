package ai.maum.chathub.api.chat.dto.maumai;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MaumAIConfig {
    private Double topP;
    private Double topK;
    private Double temperature;
    private Double frequencyPenalty;
    private Double presencePenalty;
    private Double beam_width;
    private Double penalty_alpha;
}
