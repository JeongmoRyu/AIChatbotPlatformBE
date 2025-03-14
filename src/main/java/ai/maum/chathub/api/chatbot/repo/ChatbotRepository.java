package ai.maum.chathub.api.chatbot.repo;

import ai.maum.chathub.api.chatbot.entity.ChatbotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatbotRepository extends JpaRepository<ChatbotEntity, Long> {
    List<ChatbotEntity> findChatbotEntitiesByUserId(String userId);
    List<ChatbotEntity> findChatbotEntitiesByUserIdAndId(String userId, Long id);

    boolean existsByUserIdAndId(String userId, Long id);
    int deleteChatbotEntityByUserIdAndId(String userId, Long id);

    ChatbotEntity findChatbotEntityById(Long id);

    //    deleteById(Long id);
}
