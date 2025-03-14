package ai.maum.chathub.api.agentchat.dto;

import lombok.Getter;
import lombok.Setter;
import rag_service.rag_module.Rag;

@Getter
@Setter
public class ApiKeyInfo {
    int engindIdx;
    Rag.APIKey apiKey;

    public ApiKeyInfo(int engindIdx, Rag.APIKey apiKey) {
        this.engindIdx = engindIdx;
        this.apiKey = apiKey;
    }
}
