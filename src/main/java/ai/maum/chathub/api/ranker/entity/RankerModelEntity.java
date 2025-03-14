package ai.maum.chathub.api.ranker.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name="ranker_model")
public class RankerModelEntity {
    @Schema(description = "ID", example = "11")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

//    @Schema(description = "ranker history id", example = "22")
//    Long rankerId;

    @Schema(description = "engine id", example = "22")
    Long engineId;

    @Schema(description = "name", example = "BM25")
    String name;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;

    // 부모 엔티티와의 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ranker_id", nullable = false)
    private RankerHistoryEntity rankerHistoryEntity;

    // 자식 엔티티 리스트, cascade와 orphanRemoval 설정 추가
    @OneToMany(mappedBy = "rankerModelEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RankerModelEnsembleEntity> rankerModelEnsembleEntityList;

}
