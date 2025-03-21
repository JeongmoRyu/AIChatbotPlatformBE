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
@Table(name="chatroom")
public class ChatroomEntity {
    @Schema(description = "챗봇룸ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "챗봇ID", example = "1")
    private Long chatbotId;

    @Schema(description = "등록자ID", example = "12345")
    private String regUserId;

    @Schema(description = "순서", example = "CARD")
    private Integer seq;

    @Schema(description = "룸이름", example = "비타민C 제품문의")
    private String title;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Schema(description = "수정일", example = "2024-01-24 14:10:02")
    private Timestamp updatedAt;
}
