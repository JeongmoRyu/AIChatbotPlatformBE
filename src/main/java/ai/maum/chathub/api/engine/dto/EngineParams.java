package ai.maum.chathub.api.engine.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EngineParams {
    private List<EngineParam> paramaters;

    public EngineParams(List<EngineParam> paramaters) {
        this.paramaters = paramaters;
    }
}
