package ai.maum.chathub.api.routine.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter @Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name="routine_schedule")
public class RoutineScheduleEntity {
    @Schema(description = "루틴스케쥴ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "전송일시", example = "2024-06-04 17:20:00.000")
    private Timestamp sendTime;

    @Schema(description = "TASK 코드", example = "DM-P-1")
    private String taskCd;

    @Schema(description = "수신자번호", example = "01012345678")
    private String receiveId;

    @Schema(description = "전송여부", example = "N")
    private String sendYn;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Schema(description = "수정일", example = "2024-01-24 14:10:02")
    private Timestamp updatedAt;
}