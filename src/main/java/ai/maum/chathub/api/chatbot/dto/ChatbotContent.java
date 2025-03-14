package ai.maum.chathub.api.chatbot.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatbotContent {
    private ChatbotContentTitle title;
    private ChatbotContentComment comment;
    private List<ChatbotContentCard> cards;
}
