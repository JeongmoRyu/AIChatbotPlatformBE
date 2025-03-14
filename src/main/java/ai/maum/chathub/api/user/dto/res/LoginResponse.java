package ai.maum.chathub.api.user.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "로그인 응답")
public class LoginResponse {
    @Schema(title = "토큰",
            description = "사용자 정보가 존재하는 JWT 토큰이다.",
            example = "token1234@")
    private String token;
}
