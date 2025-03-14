package ai.maum.chathub.api.file.repo;

import ai.maum.chathub.api.file.entity.ChatbotFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotFileRepository extends JpaRepository<ChatbotFileEntity, Integer> {

    void deleteByChatbotId(Integer chatbotId);

    void deleteByFileId(Integer fileId);
}
