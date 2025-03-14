package ai.maum.chathub.mybatis.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitorLogVO {
    private Long roomId;
    private Long seq;
    private String log;
    private Long id;
    private String createdAt;
    private String title;
}
