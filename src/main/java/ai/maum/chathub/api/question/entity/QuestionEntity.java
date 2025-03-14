package ai.maum.chathub.api.question.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name="question_generate")
public class QuestionEntity {
    @Schema(description = "ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id;
    @Schema(description = "엔진ID", example = "22")
    @Column(name = "engine_id")
    private Long engineId;
    @Schema(description = "프롬프트", example = "넌챗봇전문가야. 챗봇명/역할을 받아서 적절한 질문을 생성해줘")
    @Column(name = "system_prompt")
    private String systemPrompt;
    @Schema(description = "순번", example = "1")
    private int seq;

    @Schema(description = "Top P", example = "0.9")
    @Column(name = "top_p")
    private Double topP;
    @Schema(description = "Temperature", example = "0.5")
    @Column(name = "temperature")
    private Double temperature;
    @Schema(description = "Presence Penalty", example = "1.0")
    @Column(name = "pres_p")
    private Double pres_p;
    @Schema(description = "Frequency Penalty", example = "1.0")
    @Column(name = "freq_p")
    private Double freq_p;
    @Schema(description = "Max Tokens", example = "2048")
    @Column(name = "max_token")
    private int max_token;



    @Schema(description = "사용여부", example = "true")
    @Column(name = "use_yn")
    private Boolean useYn;
}
