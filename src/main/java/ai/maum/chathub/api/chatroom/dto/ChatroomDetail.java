package ai.maum.chathub.api.chatroom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

@Getter
@Setter
public class ChatroomDetail {
    private Long id;
    private Long roomId;
    private Long seq;
    private String role;
    private String content;
    private String feedback;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public ChatroomDetail(Long roomId, Long seq, String role, String content) {
        this.roomId = Long.valueOf(roomId);
        this.seq = Long.valueOf(seq);
        this.role = String.valueOf(role);
        this.content = String.valueOf(content);
    }

    public ChatroomDetail() {
    }
}
