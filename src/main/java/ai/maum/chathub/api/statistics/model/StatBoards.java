package ai.maum.chathub.api.statistics.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatBoards {
    private StatBoard apiRequest;
    private StatBoard tokens;
}
