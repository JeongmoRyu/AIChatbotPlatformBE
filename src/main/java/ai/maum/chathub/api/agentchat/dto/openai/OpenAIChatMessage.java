package ai.maum.chathub.api.agentchat.dto.openai;

import ai.maum.chathub.api.agentchat.dto.ChatMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAIChatMessage {
    private Integer id; // Optional
    private String role; // "system", "assistant", "user"
    private String content;

    public OpenAIChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public OpenAIChatMessage(ChatMessage chatMessage) {
        this.setId(chatMessage.getId());
        this.setRole(chatMessage.getRole());
        this.setContent(chatMessage.getContent());
    }
//    public OpenAIChatMessage(String role, String content, Integer seq) {
//        this.role = role;
//        this.content = content;
//        this.seq = 0;
//    }
}
