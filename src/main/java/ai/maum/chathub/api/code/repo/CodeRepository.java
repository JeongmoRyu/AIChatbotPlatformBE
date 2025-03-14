package ai.maum.chathub.api.code.repo;

import ai.maum.chathub.api.code.dto.res.CodeRes;
import ai.maum.chathub.api.code.entity.CodeEntity;
import ai.maum.chathub.api.code.entity.CodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeRepository  extends JpaRepository<CodeEntity, CodeId> {
    @Query("SELECT new ai.maum.chathub.api.code.dto.res.CodeRes(c.codeId.cdId, c.name) FROM CodeEntity c WHERE c.codeId.cdgroupId = :cdGroupId")
    List<CodeRes> findByCdGroupId(String cdGroupId);
}
