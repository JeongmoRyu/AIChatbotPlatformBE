package ai.maum.chathub.conf.security;

import ai.maum.chathub.util.JwtUtil;
import ai.maum.chathub.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * multipart/form-data 요청 구현체
 * @author baekgol
 */
@Getter
public class MultipartHttpServletRequest extends StandardMultipartHttpServletRequest implements HttpServletRequestSupport {
    private final boolean isAuth;
    @Setter
    private List<GrantedAuthority> roles;
    private final Map<String, String[]> parameterMap;
    private final Set<String> multipartParameterNames;
    private final MultiValueMap<String, MultipartFile> multipartFiles;

    public MultipartHttpServletRequest(HttpServletRequest request, boolean isAuth) {
        super(request);
        this.isAuth = isAuth;
        parameterMap = new HashMap<>(request.getParameterMap());
        multipartFiles = new LinkedMultiValueMap<>(super.getMultipartFiles());
        multipartParameterNames = new HashSet<>(parameterMap.keySet());
        reload(parameterMap);
        reload(multipartFiles);
    }

    @Override
    public String getParameter(String name) {
        String[] param = parameterMap.get(name);
        return param != null ? param[0] : null;
    }

    @NotNull
    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(multipartParameterNames);
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

    private <T> void reload(Map<String, T> info) {
        Map<String, String> keys = new HashMap<>();
        List<String> emptyKeys = new ArrayList<>();

        for(String key: info.keySet()) {
            String newKey = StringUtil.convertNaming(key, false);
            if((info instanceof HashMap && ((String[]) info.get(key))[0].isEmpty())
                    ||((info.get(key)).equals(""))) emptyKeys.add(key);
            else if(!key.equals(newKey)) keys.put(key, newKey);
        }

        for(String key: keys.keySet()) {
            info.put(keys.get(key), info.get(key));
            info.remove(key);

            if(info instanceof HashMap) {
                multipartParameterNames.add(keys.get(key));
                multipartParameterNames.remove(key);
            }
        }

        for(String key: emptyKeys) {
            info.remove(key);
            multipartParameterNames.remove(key);
        }
    }
}
