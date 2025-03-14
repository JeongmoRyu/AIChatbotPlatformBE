package ai.maum.chathub.api.admin.repo;

import ai.maum.chathub.api.admin.entity.TempMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TempMessageRepository extends JpaRepository<TempMessageEntity, Long> {
    List<TempMessageEntity> findTop5TempMessageEntitiesByBotUserKeyOrderByIdDesc(String botUserKey);
    //    deleteById(Long id);
}
