package ai.maum.chathub.api.agentchat.dto.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private Object systemFingerprint;
    private List<Choice> choices;
    private String serviceTier;

    // choices 배열 내의 항목을 나타내는 객체
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private int index;
        private Delta delta;
        private Object logprobs;
        private Object finishReason;
        private List<Message> messages;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private Delta delta;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    // delta 객체
    public static class Delta {
        private String role;
        private String content;

        // getters and setters
    }
}
