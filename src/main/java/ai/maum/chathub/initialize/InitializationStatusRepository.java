package ai.maum.chathub.initialize;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InitializationStatusRepository extends JpaRepository<InitializationStatus, Long> {
}