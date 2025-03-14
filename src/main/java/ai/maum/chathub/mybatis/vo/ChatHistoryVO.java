package ai.maum.chathub.mybatis.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatHistoryVO {
    private Long seq;
    private String input;
    private String output;
}
