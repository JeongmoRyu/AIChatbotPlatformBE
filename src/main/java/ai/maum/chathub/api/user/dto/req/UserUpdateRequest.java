package ai.maum.chathub.api.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "사용자 정보 수정 요청")
public class UserUpdateRequest {
    @Schema(title = "사용자 계정", example = "abc")
    private String account;
}
