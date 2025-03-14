package ai.maum.chathub.api.code.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
public class CodeId implements Serializable {
    private String cdgroupId;
    private String cdId;

    public CodeId() {}

    public CodeId(String cdgroupId, String cdId) {
        this.cdgroupId = cdgroupId;
        this.cdId = cdId;
    }

    // 게터, 세터, equals(), hashCode() 메소드
    // equals()와 hashCode()는 복합 키의 동등성을 올바르게 처리하기 위해 반드시 구현해야 합니다.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CodeId codeId = (CodeId) o;

        if (!cdgroupId.equals(codeId.cdgroupId)) return false;
        return cdId.equals(codeId.cdId);
    }

    @Override
    public int hashCode() {
        int result = cdgroupId.hashCode();
        result = 31 * result + cdId.hashCode();
        return result;
    }

    // 게터 및 세터 생략
}
