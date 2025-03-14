package ai.maum.chathub.api.member.repo;


import ai.maum.chathub.api.member.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {

    // 조직명으로 조직 조회
    Optional<OrganizationEntity> findByName(String name);
}