package ai.maum.chathub.api.chatbot.entity;

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
@Table(name="chatbot_contents")
public class ChatbotContentEntity {

    @Schema(description = "챗봇컨텐트ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "챗봇ID", example = "1")
    private Long chatbotId;

    @Schema(description = "컨텐트타입(TITLE,COMMENT,CARD)", example = "CARD")
    private String typeCd;

    @Schema(description = "순서", example = "1")
    private Integer seq;

    @Schema(description = "이미지 경로", example = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT4rtOch_MnlcVhUzWmwyi8hQiM6be0k-0f9A&usqp=CAU")
    private String img;

    @Schema(description = "컨텐트 제목", example = "까꿍 놀이 문의.")
    private String title;

    @Schema(description = "컨텐트 내용", example = "까꿍 놀이가 뭔가요? 까꿍 놀이 방법에 대해 알려 주세요. ")
    private String text;

    @Schema(description = "등록자ID", example = "12345")
    private String regUserId;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Schema(description = "수정일", example = "2024-01-24 14:10:02")
    private Timestamp updatedAt;

}
