package ai.maum.chathub.conf.security;

import ai.maum.chathub.util.JwtUtil;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * 일반 요청 구현체
 * @author baekgol
 */
public class GeneralHttpServletRequest extends HttpServletRequestWrapper implements HttpServletRequestSupport {
    private final boolean isAuth;
    @Setter
    private List<GrantedAuthority> roles;

    public GeneralHttpServletRequest(HttpServletRequest request, boolean isAuth) {
        super(request);
        this.isAuth = isAuth;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        if(isAuth && value != null && HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name))
            return JwtUtil.addRoles(value.replace("Bearer ", ""), roles);
        return value;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> headers = Collections.list(super.getHeaders(name));
        if(!headers.isEmpty()) headers.set(0, getHeader(name));
        return Collections.enumeration(headers);
    }

    @Override
    public String getOriginalToken() {
        return super.getHeader(HttpHeaders.AUTHORIZATION);
    }
}
