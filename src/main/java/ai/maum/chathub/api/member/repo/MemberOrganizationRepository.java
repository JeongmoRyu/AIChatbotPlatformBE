package ai.maum.chathub.api.member.repo;

import ai.maum.chathub.api.member.entity.MemberOrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberOrganizationRepository extends JpaRepository<MemberOrganizationEntity, MemberOrganizationEntity> {
    List<MemberOrganizationEntity> findByMember_UserKey(Long memberId);
    List<MemberOrganizationEntity> findByOrganization_Id(Long organizationId);

    // ✅ 특정 멤버가 속한 조직 중 하나의 조직 ID만 가져오기
    Optional<MemberOrganizationEntity> findFirstByMember_UserKeyOrderByOrganization_IdAsc(Long memberId);

    @Query("SELECT mo.organization.name FROM MemberOrganizationEntity mo WHERE mo.id.memberId = :memberId ORDER BY mo.organization.id ASC")
    Optional<String> findFirstOrganizationNameByMemberId(@Param("memberId") Long memberId);

//    // 특정 멤버가 속한 조직 중 하나의 조직 ID만 가져오기
//    @Query("SELECT mo.organization.id FROM MemberOrganizationEntity mo WHERE mo.id.memberId = :memberId ORDER BY mo.organization.id ASC LIMIT 1")
//    Long findFirstOrganizationIdByMemberId(@Param("memberId") Long memberId);

    // 멤버를 조직에서 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM MemberOrganizationEntity mo WHERE mo.id.memberId = :memberId AND mo.id.organizationId = :organizationId")
    void deleteByMemberIdAndOrganizationId(@Param("memberId") Long memberId, @Param("organizationId") Long organizationId);
}
