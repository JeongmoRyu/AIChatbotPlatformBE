package ai.maum.chathub.api.chatroom.entity;

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
@Table(name="chatroom_detail")
public class ChatroomDetailEntity {
    @Schema(description = "챗봇룸목록ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "챗봇룸ID", example = "1")
    private Long roomId;

    @Schema(description = "순서", example = "CARD")
    private Long seq;

    @Schema(description = "ROLE", example = "user/assistant")
    private String role;

    @Schema(description = "채팅내용", example = "까꿍 놀이는 보호자가 아기의 얼굴을 가린 후 다시 보여주는 놀이입니다. 단순한 놀이이지만 아기의 인지 발달, 사회적 상호작용, 정서 발달에 중요한 역할을 합니다.")
    private String content;

    @Schema(description = "피드백", example = "대답 꼬라지 하고는.")
    private String feedback;

    @Schema(description = "사용자이름", example = "챗허브어드민")
    @Column(nullable = true)
    private String userName;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Schema(description = "수정일", example = "2024-01-24 14:10:02")
    private Timestamp updatedAt;

    public ChatroomDetailEntity(Long roomId, Long seq, String role, String content) {
        this.roomId = roomId;
        this.seq = seq;
        this.role = role;
        this.content = content;
    }

    public ChatroomDetailEntity() {
    }
}
