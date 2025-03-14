package ai.maum.chathub.api.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private int expiresIn;
    private String code;
    private String message;
}
