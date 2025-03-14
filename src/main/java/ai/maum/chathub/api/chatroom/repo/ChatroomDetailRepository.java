package ai.maum.chathub.api.chatroom.repo;

import ai.maum.chathub.api.chatroom.entity.ChatroomDetailEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface ChatroomDetailRepository extends JpaRepository<ChatroomDetailEntity, Long> {

//    @Query("SELECT MAX(e.id) FROM ChatroomDetailEntity e")
    ChatroomDetailEntity findTopByOrderByIdDesc();

    List<ChatroomDetailEntity> findChatroomDetailEntitiesByRoomIdOrderBySeqAscCreatedAtAsc(Long roomId);
    @Query("SELECT MAX(c.seq) FROM ChatroomDetailEntity c where c.roomId = :roomId")
    Long findMaxMaxSeqByRoomId(@Param("id") Long roomId);

    List<ChatroomDetailEntity> findChatroomDetailEntitiesByRoomIdAndSeq(Long roomId, Long seq);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ChatroomDetailEntity c SET c.feedback = :feedback, " +
            "c.userName = COALESCE(:userName, c.userName) " +
            "WHERE c.roomId = :roomId AND c.seq = :seq AND LOWER(c.role) = LOWER(:role)")
    int updateFeedbackByRoomIdAndSeqAndRole(Long roomId, Long seq, String role, String feedback, String userName);
//    @Transactional
//    @Modifying(clearAutomatically = true)
//    @Query("UPDATE ChatroomDetailEntity c SET c.feedback = :feedback WHERE c.roomId = :roomId AND c.seq = :seq AND LOWER(c.role) = LOWER(:role)")
//    int updateFeedbackByRoomIdAndSeqAndRole(Long roomId, Long seq, String role, String feedback);

}
