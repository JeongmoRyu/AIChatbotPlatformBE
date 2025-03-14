package ai.maum.chathub.api.agentchat.dto.maumai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaumAIRagKnowlegeItem {
    private String text;
    private Double score;
    private String reference;
//    private String _reference;
    private String url;
//    private String _url;
}
