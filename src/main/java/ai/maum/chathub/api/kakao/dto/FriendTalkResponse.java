package ai.maum.chathub.api.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FriendTalkResponse {
    private String uid;
    private String cid;
    private String code;
    private String message;
    @JsonProperty("kko_status_code")
    private String kkoStatusCode;
    @JsonProperty("kko_message")
    private String kkoMessage;
}
