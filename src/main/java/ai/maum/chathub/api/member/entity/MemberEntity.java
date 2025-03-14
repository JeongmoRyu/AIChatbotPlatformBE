package ai.maum.chathub.api.member.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")
})
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_key", nullable = false)
    private Long userKey;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "password", length = 200)
    private String password;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "vendor_user_key", length = 200)
    private String vendorUserKey;

    @Column(name = "vendor_type", length = 10)
    private String vendorType;

    @Column(name = "receive_id", length = 20)
    private String receiveId;

    @Column(name = "vendor_user_id", length = 20)
    private String vendorUserId;

    @Column(name = "created_at", updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    @Column(name = "longitude")
    private Float longitude;

    @Column(name = "latitude")
    private Float latitude;

    @Column(name = "roles", length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'ROLE_USER'")
    private String roles;

    @Column(name = "default_chatbot_id")
    private Long defaultChatbotId;

    @Column(name = "birth_year", length = 4)
    private String birthYear;

    @Column(name = "sex", length = 1)
    private String sex;

    @Column(name = "use_yn")
    private String useYn;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private Set<MemberOrganizationEntity> organizations;
}