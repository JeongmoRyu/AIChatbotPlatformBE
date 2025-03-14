package ai.maum.chathub.api.agentchat.dto.openai;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OpenAIRequest extends OpenAIConfig {
    private List<OpenAIChatMessage> messages;
}
