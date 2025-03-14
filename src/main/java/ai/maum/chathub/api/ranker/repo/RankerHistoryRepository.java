package ai.maum.chathub.api.ranker.repo;

import ai.maum.chathub.api.ranker.dto.res.RankerHistoryDetailRes;
import ai.maum.chathub.api.ranker.dto.res.RankerHistoryListRes;
import ai.maum.chathub.api.ranker.entity.RankerHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

//Long id, String name, String creator, String timeStamp, Timestamp createdAt, String embeddingStatus, Long userKey, Boolean isMine
@Repository
public interface RankerHistoryRepository extends JpaRepository<RankerHistoryEntity, Long> {
    @Query("SELECT new ai.maum.chathub.api.ranker.dto.res.RankerHistoryListRes(" +
            "rh.id, " +
            "rh.name, " +
            "m.name, " + // 별칭 없이 m.name
            "CASE WHEN rh.embeddingStatus = 'P' THEN '생성중' ELSE TO_CHAR(rh.createdAt, 'YYYY-MM-DD') END, " + // 별칭 없이 CASE WHEN 구문
            "rh.createdAt, " +
            "rh.embeddingStatus, " +
            "rh.userKey, " +
            "CASE WHEN rh.userKey = :myUserKey THEN true ELSE false END) " + // 별칭 없이 CASE WHEN 구문
            "FROM RankerHistoryEntity rh " +
            "LEFT JOIN MemberEntity m ON m.userKey = rh.userKey " +
            "WHERE (:searchUserKey IS NULL OR rh.userKey = :searchUserKey)")
    Page<RankerHistoryListRes> findAllWithUserKey(@Param("myUserKey") Long userKey, @Param("searchUserKey") Long searchUserKey, Pageable pageable);
}
