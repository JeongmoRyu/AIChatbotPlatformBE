package ai.maum.chathub.api.member.dto;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class MemberDetail implements UserDetails {
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean enabled;

    private String name;
    private Long userKey;
    private String userId;
    private String vendorUserKey;
    private String vendorType;
    private String vendorUserId;
    private String receiveId;

    private Float longitude;
    private Float latitude;

    private String roles;
    private Long defaultChatbotId;

    private String sex;
    private String birthYear;
    private Boolean isEditor;
    private Boolean isAdmin;
    private Boolean isSuperAdmin;

    private String useYn;

    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    public MemberDetail(String id, String name) {
        this.username = id;
        this.name = name;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<String> authorities = new ArrayList<String>();
        authorities.add("ROLE_USER");
        this.authorities = authorities
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return this.authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

//                        userList.add(new MemberDetail(id, password, name, role, sex, birthDate, phone));

    public MemberDetail(String username, String password, String name, String roles, String sex, String birthYear, String receiveId) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.receiveId = receiveId;
        this.roles = roles;
        this.sex = sex;
        this.birthYear = birthYear;
    }
}
