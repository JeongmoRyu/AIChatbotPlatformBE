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
@Table(name="routine_info")
public class RoutineInfoEntity {
    @Schema(description = "루틴ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "TASK 코드", example = "DM-P-1")
    private String taskCd;

    @Schema(description = "시나리오명", example = "루틴점검")
    private String senarioName;

    @Schema(description = "시나리오 안내 멘트", example = "○○님~ 진단시 알려드린 대로 스킨케어 루틴을 잘 지키고 계신가요?")
    private String senarioMent;

    @Schema(description = "설명", example = "데일리아침-RAG")
    private String desc;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Schema(description = "수정일", example = "2024-01-24 14:10:02")
    private Timestamp updatedAt;

    @Schema(description = "사용여부", example = "Y")
    private String useYn;

    @Schema(description = "랜덤여부", example = "Y")
    private String randomYn;

    @Schema(description = "연속발송 TASK 코드", example = "DM-P-2")
    private String nextTaskCd;

    @Schema(description = "광고여부", example = "Y")
    private String adYn;
}