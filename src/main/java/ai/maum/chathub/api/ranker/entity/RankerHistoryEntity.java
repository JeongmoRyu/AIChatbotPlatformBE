package ai.maum.chathub.api.ranker.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name="ranker_history")
public class RankerHistoryEntity {
    @Schema(description = "ID", example = "11")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Schema(description = "히스토리명", example = "text-embedding-3 small/large 비교")
    String name;

    @Schema(description = "엠비딩실행 사용자 ID", example = "12345")
    Long userKey;

    @Schema(description = "TOP_K값", example = "3")
    @Column(name = "top_k")
    Integer topK;

    @Schema(description = "Semantic Chunking 사용 여부", example = "Y/N")
    String useSemanticChunk;

    @Schema(description = "Fixed Size Chunking 사용 여부", example = "Y/N")
    String useFixedChunk;

    @Schema(description = "Fixed Chunk Size", example = "512")
    Integer fixedChunkSize;

    @Schema(description = "Chunk Overlap", example = "150")
    Integer fixedChunkOverlap;

    @Schema(description = "Semantic Chunking 타입", example = "percentile")
    String semanticChunkBpType;

    @Schema(description = "Semantic Chunking 에서 사용할 임베딩", example = "text-embedding-ada-002")
    String semanticChunkEmbedding;

    @Schema(description = "임베딩 상태", example = "C")
    String embeddingStatus;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Schema(description = "수정일", example = "2024-01-24 14:10:02")
    private LocalDateTime updatedAt;

    // 자식 엔티티 리스트, cascade와 orphanRemoval 설정 추가
    @OneToMany(mappedBy = "rankerHistoryEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RankerFileEntity> rankerFileEntityList;
    @OneToMany(mappedBy = "rankerHistoryEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RankerModelEntity> rankerModelEntityList;
    @OneToMany(mappedBy = "rankerHistoryEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RankerQaEntity> rankerQaEntityList;
    @OneToMany(mappedBy = "rankerHistoryEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RankerRankingEntity> rankerRankingEntityList;
}
