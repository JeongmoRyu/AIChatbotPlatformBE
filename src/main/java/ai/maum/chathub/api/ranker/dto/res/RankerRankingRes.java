package ai.maum.chathub.api.ranker.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RankerRankingRes {
    private Long rowNumber;
    private Long id;
    private String modelName;
    private String embeddingModelConfig;
    private Float hitAccuracy;
    private String description;
}
