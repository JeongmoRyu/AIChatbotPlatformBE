package ai.maum.chathub.api.question.repo;

import ai.maum.chathub.api.question.entity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository  extends JpaRepository<QuestionEntity, Long> {
    QuestionEntity findFirstByUseYnOrderBySeq(Boolean useYn);
}
