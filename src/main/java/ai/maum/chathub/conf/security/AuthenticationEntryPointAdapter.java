package ai.maum.chathub.conf.security;

import ai.maum.chathub.meta.CodeInfo;
import ai.maum.chathub.meta.ResponseMeta;
import ai.maum.chathub.meta.SecurityMeta;
import ai.maum.chathub.util.ResponseUtil;
import ai.maum.chathub.api.common.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 인증에 실패했을 경우 처리하는 서비스
 * @author baekgol
 */
@Slf4j
public class AuthenticationEntryPointAdapter implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        Exception e = (Exception)authException.getCause();

        if(e instanceof BaseException) {

            String scheme = request.getHeader("X-Forwarded-Proto"); // 클라이언트 요청 스키마 (http/https)
            String host = request.getHeader("Host"); // 클라이언트 요청 도메인과 포트
            int port = request.getServerPort();

            log.debug("request info: {} {} {}", scheme, host, port);

            if (scheme == null) {
                scheme = request.getScheme(); // 기본적으로 HttpServletRequest에서 스키마 가져오기
                log.debug("scheme is null");
            }
            log.debug("scheme : {}", scheme);

            if (host == null) {
                host = request.getServerName() + ":" + request.getServerPort(); // 기본 호스트와 포트 가져오기
                log.debug("host is null");
            } else if (!host.contains(":")) {
                if (!((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443))) {
                    host += ":" + port; // 기본 포트가 아닌 경우 포트 추가
                }
            }
            log.debug("host : {}", host);

            String redirectUrl;
            CodeInfo info = ((BaseException)e).getInfo();
            if(info.equals(ResponseMeta.UNAUTHORIZED_DOCUMENT))
//                redirectUrl = request.getContextPath() + SecurityMeta.DOCUMENT_LOGIN_URL;
                redirectUrl = scheme + "://" + host + SecurityMeta.DOCUMENT_LOGIN_URL;
            else if(info.equals(ResponseMeta.UNAUTHORIZED_ADMIN))
//                redirectUrl = request.getContextPath() + SecurityMeta.DOCUMENT_LOGIN_URL + "?type=admin";
                redirectUrl = scheme + "://" + host + SecurityMeta.DOCUMENT_LOGIN_URL + "?type=admin";
            else
//                redirectUrl = request.getContextPath() + SecurityMeta.DOCUMENT_URL;
                redirectUrl = scheme + "://" + host + SecurityMeta.DOCUMENT_URL;
            log.debug("redirectUrl : {}", redirectUrl);
            response.sendRedirect(redirectUrl);
        }
        else ResponseUtil.success(response, ResponseMeta.UNAUTHORIZED);
    }
}
