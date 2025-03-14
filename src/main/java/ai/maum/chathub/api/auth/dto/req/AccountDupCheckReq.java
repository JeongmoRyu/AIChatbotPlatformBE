package ai.maum.chathub.api.auth.dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDupCheckReq {
    private String email;
}
