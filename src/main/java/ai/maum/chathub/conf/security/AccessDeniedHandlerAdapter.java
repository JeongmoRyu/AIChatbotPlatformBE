package ai.maum.chathub.conf.security;

import ai.maum.chathub.meta.ResponseMeta;
import ai.maum.chathub.util.ResponseUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 권한이 거부되었을 경우 처리하는 서비스
 * @author baekgol
 */
public class AccessDeniedHandlerAdapter implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        ResponseUtil.success(response, ResponseMeta.ACCESS_DENIED);
    }
}
