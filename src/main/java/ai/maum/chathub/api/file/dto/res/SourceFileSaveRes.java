package ai.maum.chathub.api.file.dto.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceFileSaveRes {

    private Integer id;
    private String name;
    private String orgName;
    private String userName;
    private String createdAt;
    private String path;
    private Long size;
}
