package ai.maum.chathub.api.chatbot.repo;

import ai.maum.chathub.api.chatbot.entity.ChatbotContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatbotContentRepository  extends JpaRepository<ChatbotContentEntity, Long>  {
    List<ChatbotContentEntity> findByChatbotIdOrderByTypeCdAscSeqAsc(Long id);
}
