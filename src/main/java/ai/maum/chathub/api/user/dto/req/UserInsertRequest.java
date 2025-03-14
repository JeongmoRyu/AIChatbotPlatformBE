package ai.maum.chathub.api.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "사용자 등록 요청")
public class UserInsertRequest {
    @Schema(title = "사용자 계정", example = "abc")
    private String account;
    @Schema(title = "사용자 비밀번호",
            description = "SHA-512 암호화된 비밀번호",
            example = "1186964bbc221be8188b8b19114a6379e7c0b088252d2517d80fb36986888d31ef122c0ec96667977fa976f17d72f7302d175cd5472539be50a1c5015967f344")
    private String password;
}
