package ai.maum.chathub.api.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Service
public class TelegramService {
//    @Value("${telegram.bot.token}")
    private String botToken = "tokens";

    private final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

    public String sendMessage(String chatId, String messageText) {
        String url = TELEGRAM_API_URL + botToken + "/sendMessage";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format("{\"chat_id\":\"%s\",\"text\":\"%s\"}", chatId, messageText);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return "Message sent successfully!";
        } else {
            return "Failed to send message: " + response.getBody();
        }
    }
}
