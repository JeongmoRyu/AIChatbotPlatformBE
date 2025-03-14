package ai.maum.chathub.api.chatbot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatbotContentCard {
    private String img;
    private String title;
    private String text;

    public ChatbotContentCard(String img, String title, String text) {
        this.img = img;
        this.title = title;
        this.text = text;
    }
}
