package ai.maum.chathub.api.ranker.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
public class RankerHistoryListRes {
    private Long rowNumber;
    private Long id;
    private String name;
    private String creator;
    private String timeStamp;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime createdAt;
    private String embeddingStatus;
    private Long userKey;
    private Boolean isMine;

    public RankerHistoryListRes(Long rowNumber, Long id, String name, String creator, String timeStamp, LocalDateTime createdAt, String embeddingStatus, Long userKey, Boolean isMine) {
        this.rowNumber = rowNumber;
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.timeStamp = timeStamp;
        this.createdAt = createdAt;
        this.embeddingStatus = embeddingStatus;
        this.isMine = isMine;
        this.userKey = userKey;
    }

    public RankerHistoryListRes(Long id, String name, String creator, String timeStamp, LocalDateTime createdAt, String embeddingStatus, Long userKey, Boolean isMine) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.timeStamp = timeStamp;
        this.createdAt = createdAt;
        this.embeddingStatus = embeddingStatus;
        this.isMine = isMine;
        this.userKey = userKey;
    }
}
