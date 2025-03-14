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
@Table(name="routine_detail")
public class RoutineDetailEntity {
    @Schema(description = "루틴상세ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "TASK 코드", example = "DM-P-1")
    private String taskCd;

    @Schema(description = "순서", example = "1")
    private String seq;

    @Schema(description = "타입", example = "BUTTON/IMG/LINK")
    private String type;

    @Schema(description = "노출문구", example = "재방문 예약하기")
    private String text;

    @Schema(description = "모바일링크", example = "https://maum.ai")
    private String link;

    @Schema(description = "PC링크", example = "https://maum.ai")
    private String linkPc;
}