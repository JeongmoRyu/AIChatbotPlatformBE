package ai.maum.chathub.api.agentchat.service;

import ai.maum.chathub.api.agentchat.dto.openai.OpenAIChatMessage;
import ai.maum.chathub.api.agentchat.dto.openai.OpenAIRequest;
import ai.maum.chathub.api.agentchat.dto.openai.OpenAIResponse;
import ai.maum.chathub.api.chatplay.dto.req.LlmRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class AgentOpenAIChatService {
    @Value("${service.openai.api-key}")
    private String openAiApiKey;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void processOpenAIStream(LlmRequest llmRequest, SocketIOClient client) {
        try {
            // OpenAI 요청 JSON 생성
//            String requestBody = "{"
//                    + "\"model\": \"gpt-4o\","
//                    + "\"messages\": ["
//                    + "    {\"role\": \"system\", \"content\": \"You are a helpful assistant.\"},"
//                    + "    {\"role\": \"user\", \"content\": \"" + prompt + "\"}"
//                    + "],"
//                    + "\"max_tokens\": 1000,"
//                    + "\"temperature\": 0.7,"
//                    + "\"stream\": true"
//                    + "}";

            String prompt = llmRequest.getPrompt();
            String model = llmRequest.getModel()==null||llmRequest.getModel().isEmpty()?"gpt-4o": llmRequest.getModel();

            List<OpenAIChatMessage> messages = new ArrayList<OpenAIChatMessage>();
            messages.add(new OpenAIChatMessage("system", "You are a helpful assistant."));
            messages.add(new OpenAIChatMessage("user", prompt));

            OpenAIRequest payload = new OpenAIRequest();
            payload.setModel(model);
            payload.setMessages(messages);
            payload.setStream(true);

            String requestBody = objectMapper.writeValueAsString(payload);

            // HTTP 요청 생성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 비동기로 요청 실행
            CompletableFuture<HttpResponse<java.io.InputStream>> responseFuture = httpClient.sendAsync(
                    request, HttpResponse.BodyHandlers.ofInputStream()
            );

            responseFuture.thenAccept(response -> {
                if (response.statusCode() != 200) {
                    log.error("OpenAI API error: {}", response.statusCode());
                    client.sendEvent("llm_error", "OpenAI API error: " + response.statusCode());
                    return;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // WebSocket 클라이언트로 스트리밍 데이터 전송
//                        log.info("Streaming line to client: {}", line);
//                        client.sendEvent("llm_response", line);


                        if (line.startsWith("data: {")) {
                            String jsonString = line.substring(6).trim(); // "data: " 이후의 JSON 추출
                            OpenAIResponse openAIResponse = objectMapper.readValue(jsonString, OpenAIResponse.class);

                            // content 추출 및 클라이언트로 전송
                            if (openAIResponse.getChoices() != null) {
                                for (OpenAIResponse.Choice choice : openAIResponse.getChoices()) {
                                    if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                                        String content = choice.getDelta().getContent();
                                        log.debug("Streaming content to client: {}", content);
                                        client.sendEvent("llm_response", content);
                                    }
                                }
                            }
                        }

                    }
                } catch (Exception e) {
                    log.error("Error processing OpenAI stream: {}", e.getMessage());
                    client.sendEvent("llm_error", "Error processing OpenAI stream");
                }
            }).exceptionally(ex -> {
                log.error("Failed to send OpenAI request: {}", ex.getMessage());
                client.sendEvent("llm_error", "Failed to process OpenAI request");
                return null;
            });
        } catch (Exception e) {
            log.error("Error during OpenAI request setup: {}", e.getMessage());
            client.sendEvent("llm_error", "Failed to send OpenAI request");
        }
    }
}
