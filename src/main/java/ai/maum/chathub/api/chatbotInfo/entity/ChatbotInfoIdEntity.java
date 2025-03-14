package ai.maum.chathub.api.chatbotInfo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name="chatbot_info")
public class ChatbotInfoIdEntity {
    @Schema(description = "유저ID", example = "10000000001")
    private String userId;

    @Schema(description = "챗봇ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "name", example = "테스트챗봇")
    private String name;

    @Schema(description = "메모리타입", example = "1")
    private String memoryTypeCd;

    @Schema(description = "윈도우사이즈", example = "5")
    private Integer windowSize;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Schema(description = "수정일", example = "2024-01-24 14:10:02")
    private Timestamp updatedAt;

    @Schema(description = "description", example = "챗봇 설명")
    private String description;

    @Schema(description = "이미지파일ID", example = "1")
    private Long imgfileId;

    @Schema(description = "엠베딩상태", example = "C(완료)/P(진행중)")
    private String embeddingStatus;

    @Schema(description = "공개여부", example = "Y/N")
    private String publicUseYn;

    @Schema(description = "사용여부(삭제여부)", example = "Y/N")
    private String useYn;
}
