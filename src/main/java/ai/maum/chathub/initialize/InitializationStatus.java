package ai.maum.chathub.initialize;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class InitializationStatus {
    @Id
    private Long id = 1L; // 단일 레코드 관리
    private boolean isInitialized = false;
    private LocalDateTime lastInitializedAt;
}