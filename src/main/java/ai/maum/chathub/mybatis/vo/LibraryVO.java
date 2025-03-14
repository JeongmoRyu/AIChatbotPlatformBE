package ai.maum.chathub.mybatis.vo;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
public class LibraryVO {
    private Long id;
    private String name;
    private String description;
    private String imgPath;
    private String link;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
