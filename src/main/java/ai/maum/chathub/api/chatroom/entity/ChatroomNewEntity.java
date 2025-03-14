package ai.maum.chathub.api.chatroom.entity;

import lombok.Getter;
import lombok.Setter;
import ai.maum.chathub.api.chatroom.entity.ChatroomEntity;
import java.sql.Timestamp;

@Getter
@Setter
public class ChatroomNewEntity {
    private Long id;
    private Long chatbotId;
    private String regUserId;
    private Integer seq;
    private String title;
    private java.sql.Timestamp createdAt;
    private java.sql.Timestamp updatedAt;

    // Constructor to convert from entity
    public ChatroomNewEntity(ChatroomEntity entity) {
        this.id = entity.getId();
        this.chatbotId = entity.getChatbotId();
        this.regUserId = entity.getRegUserId();
        this.seq = entity.getSeq();
        this.title = entity.getTitle();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

    // No-args constructor
    public ChatroomNewEntity() {
    }
}
