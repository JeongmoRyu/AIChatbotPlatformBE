package ai.maum.chathub.api.file.repo;

import ai.maum.chathub.api.file.entity.SourceFileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceFileRepository extends JpaRepository<SourceFileEntity, Integer> {

    @Query(
            value = "SELECT" +
                    "   ROW_NUMBER() OVER (ORDER BY s.id) AS row_num" +
                    "   ,s.id AS file_id " +
                    "   ,s.org_name AS file_name " +
                    "   ,DATE_FORMAT(s.created_at, '%Y.%m.%d %H:%i') AS created_at " +
                    "   ,s.user_name " +
                    "   ,c.chatbot_id " +
                    "   ,s.size " +
                    "FROM source_file s " +
                    "LEFT JOIN (SELECT * FROM chatbot_file where chatbot_id = :chatbotId) c " +
                    "   ON s.id = c.file_id " +
                    "WHERE " +
                    "   s.user_id = :userId " +
                    "ORDER BY s.id DESC ",
            countQuery = "SELECT " +
                            "COUNT(s.id) " +
                        "FROM source_file s " +
                        "LEFT JOIN chatbot_file c " +
                            "ON s.id = c.file_id " +
                        "WHERE " +
                            "    (c.chatbot_id IS NULL OR c.chatbot_id = :chatbotId) " +
                            "    AND s.user_id = :userId ",
            nativeQuery = true
    )
    Page<Object[]> findFileList(Integer chatbotId, String userId, Pageable pageable);

    @Query(
            value = "SELECT sf.id, sf.org_name, sf.size, sf.type, sf.created_at, " +
                           "'' as user_id, '' as user_name, '' as name, '' as path " +
                    "FROM source_file sf " +
                    "LEFT JOIN function_file ff ON ff.file_id = sf.id " +
                    "WHERE ff.function_id = :functionId",
            nativeQuery = true
    )
    List<SourceFileEntity> findSourceFileEntitiesByFunctionId(Long functionId);
}
