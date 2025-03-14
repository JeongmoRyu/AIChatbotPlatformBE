package ai.maum.chathub.api.chatbot.entity;

import ai.maum.chathub.api.question.dto.Question;
import ai.maum.chathub.api.question.dto.QuestionListConverter;
import ai.maum.chathub.api.engine.dto.EngineParam;
import ai.maum.chathub.api.engine.dto.EngineParamsConverter;
import ai.maum.chathub.api.engine.entity.EngineEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name="chatbot")
public class ChatbotEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RetrieverEngineId", referencedColumnName = "id", insertable=false, updatable=false)
    private EngineEntity retrieverEngine;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "llmEngineId", referencedColumnName = "id", insertable=false, updatable=false)
    private EngineEntity llmEngine;

    public String getRetrieverEngineVendor() {
        try {
            return this.retrieverEngine != null ? this.retrieverEngine.getVendor() : "";
        } catch (Exception e) {
            return "";
        }
    }

    public String getLlmEngineVendor() {
        try {
            return this.llmEngine != null ? this.llmEngine.getVendor() : "";
        } catch (Exception e) {
            return "";
        }
    }

    @Schema(description = "사용자ID", example = "65a745060994ff6c610085b4")
    private String userId;

    @Schema(description = "챗봇ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long  id;

    @Schema(description = "챗봇명", example = "인사챗봇")
    private String name;

    @Schema(description = "챗봇유명(RAG/LLM)", example = "RAG")
    private String chatbotTypeCd;

    @Schema(description = "프롬프트-역할", example = "너는 마음AI의 챗플레이 개발 담당자야. 챗플레이 개발 관련한 질문에 대해 기술적인 조언을 해줘.")
    private String promptRole;

    @Schema(description = "프롬프트-요구사항", example = "개발외의 인사,재무등에 대한 질문에 대해서는 대답할 수 없다고 답하고, 욕설등 부적절한 질문에 대해서는 잘 모르겠다고 답해줘.")
    private String promptRequirement;

    @Schema(description = "프롬프트-RAG이후 답변", example = "개발외의 인사,재무등에 대한 질문에 대해서는 대답할 수 없다고 답하고, 욕설등 부적절한 질문에 대해서는 잘 모르겠다고 답해줘.")
    private String promptTail;

    @Schema(description = "RAG엔진ID", example = "1234")
    private Long RetrieverEngineId;

    @Schema(description = "LLM엔진ID", example = "2")
    private Long llmEngineId;

    @Schema(description = "TAIL엔진ID", example = "2")
    private Long tailEngineId;

    @Schema(description = "RAG파라미터(JSON)", example = "")
    @Convert(converter = EngineParamsConverter.class)
    private List<EngineParam> ragParameters;

    @Schema(description = "LLM파라미터(JSON)", example = "")
    @Convert(converter = EngineParamsConverter.class)
    private List<EngineParam> llmParameters;

    @Schema(description = "TAIL파라미터(JSON)", example = "")
    @Convert(converter = EngineParamsConverter.class)
    private List<EngineParam> tailParameters;

    @Schema(description = "질문 목록(JSON)", example = "[{\"question\": \"피부 타입별 추천 제품이 궁금해요.\"}, ...]")
    @Convert(converter = QuestionListConverter.class)
    private List<Question> questions;

    @Schema(description = "(테스트용 clientInfo)", example = "{\"name\":\"아모레\", \"age\":\"27\", \"sex\":\"female\"}")
    private String clientInfo;
    @Schema(description = "(테스트용 clientInfo)", example = "{\"name\":\"아모레\", \"age\":\"27\", \"sex\":\"female\"}")
    private Integer multiTurn;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Schema(description = "수정일", example = "2024-01-24 14:10:02")
    private Timestamp updatedAt;
}
