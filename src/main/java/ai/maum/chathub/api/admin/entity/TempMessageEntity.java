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
@Table(name="temp_message")
public class TempMessageEntity {
    @Schema(description = "ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "bot사용자키", example = "kljdfhkalfhjaslfd")
    private String botUserKey;

    @Schema(description = "메시지", example = "어쩌구")
    private String message;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    @Column(updatable = false)
    private Timestamp createdAt;
}
