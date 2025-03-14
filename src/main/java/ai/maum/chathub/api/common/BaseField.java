package ai.maum.chathub.api.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * 공통 엔티티 속성
 * created/updated 날짜 field가 필요한 Entity를 생성할 때, 이 클래스를 extends하여 구현한다.
 * @author bhr
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BaseField {
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @LastModifiedBy
    private String lastModifiedBy;
}
