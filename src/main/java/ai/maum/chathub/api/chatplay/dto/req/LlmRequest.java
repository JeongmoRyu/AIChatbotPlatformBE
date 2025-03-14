package ai.maum.chathub.api.chatplay.dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LlmRequest {
    private String language;
    private String prompt;
    private String model;

    @Override
    public String toString() {
        return "LlmRequest{" +
                "language='" + language + '\'' +
                ", prompt='" + prompt + '\'' +
                ", model='" + model + '\'' +
                '}';
    }
}