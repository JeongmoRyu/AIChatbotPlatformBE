package ai.maum.chathub.api.auth.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
public class MemberForMemberList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_key")
    private Long userKey;

    private String username;
    private String name;
    @Column(name = "birth_year")
    private String birthYearStr;

    @Transient
    private Integer birthYear;

    private String sex;
    private String roles;

    @Column(name = "use_yn")
    private String useYn;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Transient
    @JsonProperty("is_editor")
    private boolean isEditor;

    @Transient
    @JsonProperty("is_admin")
    private boolean isAdmin;

    @Transient
    @JsonProperty("is_super_admin")
    private boolean isSuperAdmin;

    // ✅ 조직 이름을 저장할 필드 추가
    @Transient
    private String organization;

    // Getter, Setter 추가

    @PostLoad
    private void postLoad() {
        this.isEditor = roles != null && roles.contains("ROLE_EDITOR");
        this.isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        this.isSuperAdmin = roles != null && roles.contains("ROLE_SUPER_ADMIN");
        if (birthYearStr != null && !birthYearStr.isEmpty()) {
            this.birthYear = Integer.valueOf(birthYearStr);
        }
    }
}
