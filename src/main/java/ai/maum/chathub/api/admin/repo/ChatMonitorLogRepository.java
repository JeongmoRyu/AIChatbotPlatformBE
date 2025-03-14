package ai.maum.chathub.api.admin.repo;

import ai.maum.chathub.api.admin.entity.ChatMonitorLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMonitorLogRepository extends JpaRepository<ChatMonitorLogEntity, Long> {
    List<ChatMonitorLogEntity> findChatMonitorLogEntitiesByRoomIdAndSeqOrderById(Long roomId, Long seq);
}
