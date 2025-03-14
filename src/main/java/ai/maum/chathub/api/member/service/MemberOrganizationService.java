package ai.maum.chathub.api.member.service;

import ai.maum.chathub.api.member.entity.MemberEntity;
import ai.maum.chathub.api.member.entity.MemberOrganizationEntity;
import ai.maum.chathub.api.member.entity.MemberOrganizationId;
import ai.maum.chathub.api.member.entity.OrganizationEntity;
import ai.maum.chathub.api.member.repo.MemberOrganizationRepository;
import ai.maum.chathub.api.member.repo.MemberRepository;
import ai.maum.chathub.api.member.repo.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberOrganizationService {

    private final MemberOrganizationRepository repository;
    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;

    // 특정 멤버가 속한 조직 목록 조회
    public List<MemberOrganizationEntity> getOrganizationsByMember(Long memberId) {
        return repository.findByMember_UserKey(memberId);
    }

    // 특정 조직에 속한 멤버 목록 조회
    public List<MemberOrganizationEntity> getMembersByOrganization(Long organizationId) {
        return repository.findByOrganization_Id(organizationId);
    }

    // 멤버를 조직에 추가
    public MemberOrganizationEntity addMemberToOrganization(Long memberId, Long organizationId) {
        MemberEntity member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("Member not found"));
        OrganizationEntity organization = organizationRepository.findById(organizationId).orElseThrow(() -> new RuntimeException("Organization not found"));

        MemberOrganizationEntity memberOrganization = new MemberOrganizationEntity(new MemberOrganizationId(memberId, organizationId), member, organization);
        return repository.save(memberOrganization);
    }

    // 멤버를 조직에서 삭제
    public void removeMemberFromOrganization(Long memberId, Long organizationId) {
        repository.deleteByMemberIdAndOrganizationId(memberId, organizationId);
    }

    public Long getMemberOrganizationId(Long userKey) {
        return repository.findFirstByMember_UserKeyOrderByOrganization_IdAsc(userKey)
                .map(memberOrganization -> memberOrganization.getOrganization().getId()) // `organization_id` 추출
                .orElse(null);
    }
}
