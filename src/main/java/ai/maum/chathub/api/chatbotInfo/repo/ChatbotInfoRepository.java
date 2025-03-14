package ai.maum.chathub.api.chatbotInfo.repo;

import ai.maum.chathub.api.chatbotInfo.entity.ChatbotInfoIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface  ChatbotInfoRepository extends JpaRepository<ChatbotInfoIdEntity, Long> {
    @Query("SELECT MAX(c.id) FROM ChatbotInfoIdEntity c")
    Long findMaxChatbotId();

    ChatbotInfoIdEntity findChatbotInfoIdEntityByUserIdAndId(String userId, Long id);
}