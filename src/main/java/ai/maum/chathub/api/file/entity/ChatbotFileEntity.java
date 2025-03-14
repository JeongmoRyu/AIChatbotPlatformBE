package ai.maum.chathub.api.file.entity;

import ai.maum.chathub.api.file.entity.id.ChatbotFileId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Getter
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "chatbot_file")
@IdClass(ChatbotFileId.class)
public class ChatbotFileEntity {
    @Id
    @Column(name = "chatbot_id")
    private Integer  chatbotId;
    @Id
    @Column(name = "file_id")
    private Integer fileId;

}
