package ai.maum.chathub.api.ranker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name="ranker_model_ensemble")
@NoArgsConstructor
public class RankerModelEnsembleEntity {
    @Schema(description = "ID", example = "11")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

//    @Schema(description = "Model ID", example = "11")
//    Long modelId;

    @Schema(description = "Engine ID", example = "11")
    Long engineId;

    @Schema(description = "이름", example = "BM25")
    String name;

    @Schema(description = "비중", example = "0.3")
    Double weight;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;

    // 부모 엔티티와의 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    @JsonIgnore
    private RankerModelEntity rankerModelEntity;


//    public RankerModelEnsembleEntity(Long modelId, String name, Double weight) {
//        this.modelId = modelId;
//        this.name = name;
//        this.weight = weight;
//    }
    public RankerModelEnsembleEntity(RankerModelEntity rankerModelEntity, String name, Double weight) {
        this.rankerModelEntity = rankerModelEntity;
        this.name = name;
        this.weight = weight;
    }
}
