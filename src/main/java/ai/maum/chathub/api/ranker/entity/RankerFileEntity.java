package ai.maum.chathub.api.ranker.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@Table(name="ranker_file")
public class RankerFileEntity {
    @Schema(description = "ID", example = "11")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

//    @Schema(description = "RankerHistory ID", example = "22")
//    Long rankerId;

    @Schema(description = "파일명", example = "사규v1.0.pdf")
    String name;

    @Schema(description = "파일사이즈", example = "2048")
    Long size;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;

    // 부모 엔티티와의 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ranker_id", nullable = false)
    private RankerHistoryEntity rankerHistoryEntity;

//    public RankerFileEntity(Long rankerId, String name, Long size) {
//        this.rankerId = rankerId;
//        this.name = name;
//        this.size = size;
//    }

    public RankerFileEntity(RankerHistoryEntity rankerHistoryEntity, String name, Long size) {
        this.rankerHistoryEntity = rankerHistoryEntity;
        this.name = name;
        this.size = size;
    }

    public RankerFileEntity() {

    }


}
