package ai.maum.chathub.api.code.entity;

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
@Table(name="code")
public class CodeEntity {

    @EmbeddedId
    private CodeId codeId; // 복합 키 사용

    @Schema(description = "코드명", example = "마음AI")
    private String name;

    @Schema(description = "사용여부", example = "true")
    private Boolean useYn;

    @Schema(description = "삭제여부", example = "false")
    private Boolean delYn;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Schema(description = "수정일", example = "2024-01-24 14:10:02")
    private Timestamp updatedAt;
}
