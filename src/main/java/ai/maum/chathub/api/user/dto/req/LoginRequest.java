package ai.maum.chathub.api.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "로그인 요청")
public class LoginRequest {
    @Schema(title = "사용자 계정", example = "abc")
    private String account;
    @Schema(title = "사용자 비밀번호",
            description = "SHA-512 암호화된 비밀번호",
            example = "8e2dac90277fbed95b374ae851d6977c30f90fe996cb545eac8d0abef9e98a0b10a9053201830830b95b9430cf6a7657c6479611fcbe482747a621cf7affffba")
    private String password;
}
