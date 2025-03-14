package ai.maum.chathub.api.statistics.dto.res;

import ai.maum.chathub.api.statistics.model.StatBoard;
import ai.maum.chathub.api.statistics.model.StatBoards;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChathubStatsRes {
    private StatBoards llmStats;
    private StatBoards embedStats;
}
