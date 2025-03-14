package ai.maum.chathub.api.ranker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name="ranker_qa")
public class RankerQaEntity {
    @Schema(description = "ID", example = "11")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
//    Long RankerId;

    String question;
    String answer;
    Integer docId;
    String chunk;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ranker_id", nullable = false)
    @JsonIgnore
    private RankerHistoryEntity rankerHistoryEntity;
}
