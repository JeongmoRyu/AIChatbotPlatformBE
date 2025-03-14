package ai.maum.chathub.api.ranker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name="ranker_ranking")
public class RankerRankingEntity {
    @Schema(description = "ID", example = "11")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
//    Long rankerId;

    String modelName;
    String embeddingModelConfig;
    Float hitAccuracy;
    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ranker_id", nullable = false)
    @JsonIgnore
    private RankerHistoryEntity rankerHistoryEntity;
}
