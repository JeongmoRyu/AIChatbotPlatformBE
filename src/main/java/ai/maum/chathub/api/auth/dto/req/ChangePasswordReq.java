package ai.maum.chathub.api.auth.dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordReq {
    private String password;
    private String newPassword;
}
