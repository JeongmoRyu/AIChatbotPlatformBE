package ai.maum.chathub.api.chatroom.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ChatroomDateEntity {
    private List<ChatroomDateGroup> groups;
}

