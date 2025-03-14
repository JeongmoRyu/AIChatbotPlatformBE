package ai.maum.chathub.api.chatbot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatbotContentComment {
    private String img;
    private String text;

    public ChatbotContentComment(String img, String text) {
        this.img = img;
        this.text = text;
    }
}
