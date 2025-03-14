package ai.maum.chathub.api.admin.repo;

import ai.maum.chathub.api.admin.entity.TempUserMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TempUserMappingRepository extends JpaRepository<TempUserMappingEntity, Long> {
    TempUserMappingEntity findTempUserMappingEntityByUserNameAndIdentityNo(String userName, String identityNo);
    List<TempUserMappingEntity> findTempUserMappingEntitiesByUserName(String userName);
}
