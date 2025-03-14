package ai.maum.chathub.api.apiuser.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class ApiUser {
    @Schema(description = "밴더ID(API사용처ID)", example = "maum.ai")
    private String vendorId;
    @Schema(description = "API KEY", example = "00000000-1111-2222-3333-444444444444")
    private String apiKey;
    @Schema(description = "사용여부", example = "Y")
    private String useYn;
    @Schema(description = "등록자ID", example = "12345")
    private String regId;
    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    private Timestamp createdAt;
    @Schema(description = "수정일", example = "2024-01-24 14:10:02")
    private Timestamp updatedAt;
}
