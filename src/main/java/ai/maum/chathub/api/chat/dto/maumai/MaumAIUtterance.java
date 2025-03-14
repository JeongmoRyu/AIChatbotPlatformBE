package ai.maum.chathub.api.chat.dto.maumai;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MaumAIUtterance {
    private String role;
    private String content;

    public MaumAIUtterance(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
