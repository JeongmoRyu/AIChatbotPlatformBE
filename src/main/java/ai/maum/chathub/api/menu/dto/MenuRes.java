package ai.maum.chathub.api.menu.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
public class MenuRes {
    private Long id;
    private int seq;
    private String title;
    private String subTitle;
    private String description;
    private String to;
    private String menuType;
//    @JsonIgnore
    private String useYn;
//    @JsonIgnore
    private LocalDateTime createdAt;
}
