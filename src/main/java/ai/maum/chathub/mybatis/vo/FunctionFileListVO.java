package ai.maum.chathub.mybatis.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FunctionFileListVO {
    Long functionId;
    List<Long> files;

    public class DocFile {
        Long fileId;
        Long embeddingEngineId;
    }
}


