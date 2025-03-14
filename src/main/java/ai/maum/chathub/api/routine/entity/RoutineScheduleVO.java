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
public class RoutineScheduleVO {
    @Schema(description = "루틴스케쥴ID", example = "1")
    private Long id;

    @Schema(description = "전송일시", example = "2024-06-04 17:20:00.000")
    private Timestamp sendTime;

    @Schema(description = "TASK 코드", example = "DM-P-1")
    private String taskCd;

    @Schema(description = "수신자번호", example = "01012345678")
    private String receiveId;

    @Schema(description = "수신자명", example = "김개똥")
    private String name;

    @Schema(description = "전송여부", example = "N")
    private String sendYn;

    @Schema(description = "전송결과", example = "어쩌구 저쩌구")
    private String result;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    private Timestamp createdAt;

    @Schema(description = "수정일", example = "2024-01-24 14:10:02")
    private Timestamp updatedAt;
}