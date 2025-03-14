package ai.maum.chathub.api.code.dto.res;

import com.azure.core.annotation.Get;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeRes {
    private String cdId;
    private String name;

    public CodeRes(String cdId, String name) {
        this.cdId = cdId;
        this.name = name;
    }
}
