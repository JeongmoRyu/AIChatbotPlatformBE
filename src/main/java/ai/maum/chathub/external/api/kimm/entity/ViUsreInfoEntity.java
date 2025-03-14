package ai.maum.chathub.external.api.kimm.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "VI_USER_INFO")
@Getter
@Setter
public class ViUsreInfoEntity {
    @Id
    @Column(name = "USER_ID")
    private String userId;
    @Column(name = "USER_NAME")
    private String userName;
    @Column(name = "PASSWORD")
    private String password;
    @Column(name = "HOLD_OFFI")
    private String holdOffi;
    @Column(name = "HOLD_OFFI_NM")
    private String holdOffiNm;
    @Column(name = "ROLE")
    private String role;
}
