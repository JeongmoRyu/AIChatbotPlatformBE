package ai.maum.chathub.api.statistics.dto.res;

import ai.maum.chathub.api.statistics.model.StatBoard;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RankerStatsRes {
    private StatBoard apiRequest;
    private StatBoard tokens;
}
