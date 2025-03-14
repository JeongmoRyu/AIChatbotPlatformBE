package ai.maum.chathub.api.chatbot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatbotContentTitle {
    private String text;

    public ChatbotContentTitle(String text) {
        this.text = text;
    }
}
