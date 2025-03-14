package ai.maum.chathub.api.chat.dto.maumai;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MaumAIRequest {
    private String appId;
    private String name;
    private List<String> item;
    private List<MaumAIParam> param;

}
