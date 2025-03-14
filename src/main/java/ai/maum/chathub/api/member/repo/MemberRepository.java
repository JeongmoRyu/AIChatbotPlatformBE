package ai.maum.chathub.api.member.repo;


import ai.maum.chathub.api.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    // username 으로 사용자 조회
    Optional<MemberEntity> findByUsername(String username);
    // username 으로 사용자 조회
    Optional<MemberEntity> findMemberEntityByUserKey(Long userKey);
    List<MemberEntity> findMemberEntitiesByUseYn(String useYn);
}
