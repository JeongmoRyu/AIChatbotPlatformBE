package ai.maum.chathub.mybatis.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ChatbotInfoEmbeddingStats {
    private Long chatbotId;
    private Long functionId;
    private Long fileId;
    private Long embeddingEngineId;
    private String embeddingStatus;

    public ChatbotInfoEmbeddingStats(Long chatbotId, Long functionId, Long fileId, Long embeddingEngineId, String embeddingStatus) {
        this.chatbotId = chatbotId;
        this.functionId = functionId;
        this.fileId = fileId;
        this.embeddingEngineId = embeddingEngineId;
        this.embeddingStatus = embeddingStatus;
    }
}
