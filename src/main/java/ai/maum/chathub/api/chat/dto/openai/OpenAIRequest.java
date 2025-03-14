package ai.maum.chathub.api.chat.dto.openai;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OpenAIRequest extends OpenAIConfig {
    private List<OpenAIChatMessage> messages;
}
