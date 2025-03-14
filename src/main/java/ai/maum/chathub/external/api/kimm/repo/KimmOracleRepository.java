package ai.maum.chathub.external.api.kimm.repo;

import ai.maum.chathub.external.api.kimm.entity.ViUsreInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KimmOracleRepository extends JpaRepository<ViUsreInfoEntity, String> {
}
