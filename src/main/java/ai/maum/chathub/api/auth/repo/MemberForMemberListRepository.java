package ai.maum.chathub.api.auth.repo;

import ai.maum.chathub.api.auth.dto.res.MemberForMemberList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberForMemberListRepository extends JpaRepository<MemberForMemberList, Long> {
    Page<MemberForMemberList> findAll(Specification<MemberForMemberList> spec, Pageable pageable);
//    Page<MemberForMemberList> findByUsernameContaining(String email, Pageable pageable);
    Page<MemberForMemberList> findByUseYnNot(String useYn, Pageable pageable);
    Page<MemberForMemberList> findByUsernameContainingAndUseYnNot(String email, String useYn, Pageable pageable);
    Page<MemberForMemberList> findByUseYn(String useYn, Pageable pageable);
    Page<MemberForMemberList> findByUsernameContainingAndUseYn(String email, String useYn, Pageable pageable);
}