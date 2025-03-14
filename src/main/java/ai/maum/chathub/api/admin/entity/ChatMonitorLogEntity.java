package ai.maum.chathub.api.admin.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name="chat_monitor_log")
public class ChatMonitorLogEntity {
    @Schema(description = "ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "룸ID", example = "1")
    private Long roomId;

    @Schema(description = "SEQ", example = "1")
    private Long seq;

    @Schema(description = "LOG", example = "1")
    private String log;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    @Column(updatable = false)
    private Timestamp createdAt;

    @Schema(description = "제목", example = "사용자 정보 Get")
    private String title;

    @Schema(description = "토큰수", example = "1234")
    private Integer tokens;

//    public ChatMonitorLogEntity(Long roomId, Long seq, String log) {
//        this.roomId = roomId;
//        this.seq = seq;
//        this.log = log;
//    }

    public ChatMonitorLogEntity(Long roomId, Long seq, String title, String log, Integer tokens) {
        this.roomId = roomId;
        this.seq = seq;
        this.log = log;
        this.title = title;
        this.tokens = tokens;
    }
    public ChatMonitorLogEntity() {
    }
}
