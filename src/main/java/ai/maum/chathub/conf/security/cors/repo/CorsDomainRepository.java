package ai.maum.chathub.conf.security.cors.repo;

import ai.maum.chathub.conf.security.cors.entity.CorsDomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

//@Repository
//public interface CorsDomainRepository extends JpaRepository<CorsDomainEntity, Long> {
//
//    @Query("SELECT c.domain FROM CorsDomainEntity c")
//    List<String> findAllDomains();
//}

@Repository
public interface CorsDomainRepository extends JpaRepository<CorsDomainEntity, Long> {
    @Query("SELECT c.domain FROM CorsDomainEntity c")
    List<String>  findAllDomains(); // 'domain' 필드 값 전체를 가져오는 메서드
}
