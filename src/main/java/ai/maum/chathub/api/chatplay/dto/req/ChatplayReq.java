package ai.maum.chathub.api.chatplay.dto.req;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class ChatplayReq {
    private String email;
    private String password;
    private String name;
    private String role;
    private String sex;
    private String birthday;
    private String phone;

    public ChatplayReq(String email, String name) {
        this.email = email;
        this.name = name;
    }
}
