package ai.maum.chathub.api.chat.dto;

import ai.maum.chathub.api.chat.dto.openai.OpenAIChatMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class  ChatMessage extends OpenAIChatMessage {

    private Long seq;
    public ChatMessage(String role, String content) {
        super(role, content);
    }
}
