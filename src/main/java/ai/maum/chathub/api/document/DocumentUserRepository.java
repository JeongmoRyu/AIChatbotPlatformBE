package ai.maum.chathub.api.document;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentUserRepository extends JpaRepository<DocumentUserEntity, Integer> {
    DocumentUserEntity findByAccountAndPassword(String account, String password);
}
