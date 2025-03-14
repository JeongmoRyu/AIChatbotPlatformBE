package ai.maum.chathub.api.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberInfo {
    //"{\"name\" : \"성진영\", \"age\" : \"30\", \"gender\" : \"male\"}"
    private String name;
    private String age;
    private String gender;
    private Float longitude;
    private Float latitude;
}
