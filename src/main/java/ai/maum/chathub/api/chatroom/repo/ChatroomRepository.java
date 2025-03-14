package ai.maum.chathub.api.chatroom.repo;

import ai.maum.chathub.api.chatroom.entity.ChatroomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatroomRepository extends JpaRepository<ChatroomEntity, Long> {
//    List<ChatroomEntity> findChatroomEntitiesByChatbotIdOrderByUpdatedAt(Long chatbotId);
    List<ChatroomEntity> findChatroomEntitiesByChatbotIdOrderBySeqAscUpdatedAtDesc(Long chatbotId);
//    List<ChatroomEntity> findChatroomEntitiesByChatbotIdAndRegUserIdOrderBySeqAscUpdatedAtDesc(Long chatbotId, String regUserId);
    List<ChatroomEntity> findByChatbotIdAndRegUserIdAndTitleIsNotNullOrderBySeqAscUpdatedAtDesc(Long chatbotId, String regUserId);

    List<ChatroomEntity> findChatroomEntitiesByRegUserId(String regUserId);

    List<ChatroomEntity> findByChatbotIdAndRegUserIdAndTitleIsNotNullOrderByUpdatedAtDesc(Long chatbotId, String regUserId);
}
