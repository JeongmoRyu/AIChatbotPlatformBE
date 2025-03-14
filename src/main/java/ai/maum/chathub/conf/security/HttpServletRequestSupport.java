package ai.maum.chathub.conf.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * 요청 인터페이스
 * @author baekgol
 */
public interface HttpServletRequestSupport {
    void setRoles(List<GrantedAuthority> roles);
    String getOriginalToken();
}
