package ai.maum.chathub.conf.security;

import ai.maum.chathub.conf.annotation.NoToken;
import ai.maum.chathub.meta.RegexMeta;
import ai.maum.chathub.meta.ResponseMeta;
import ai.maum.chathub.meta.SecurityMeta;
import ai.maum.chathub.util.JwtUtil;
import ai.maum.chathub.util.StringUtil;
import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.service.MemberDetailService;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JWT 토큰 검증 필터
 * @author baekgol
 */
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final RequestMappingHandlerMapping handlerMapping;
//    private final UserRepository userRepository;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final MemberDetailService memberDetailService;

    JwtFilter(RequestMappingHandlerMapping handlerMapping,
//              UserRepository userRepository,
              MemberDetailService memberDetailService,
              AuthenticationEntryPoint authenticationEntryPoint) {
        this.handlerMapping = handlerMapping;
//        this.userRepository = userRepository;
        this.memberDetailService = memberDetailService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getServletPath();
            HttpSession session = request.getSession(false);

            if(StringUtil.matches(path, RegexMeta.UNNECESSARY_PATHS))
                filterChain.doFilter(request, response);
//            else if(StringUtil.matches(path, RegexMeta.DOCUMENT_PATHS)) {
//                if(session == null || session.getAttribute("user") == null) throw BaseException.of(ResponseMeta.UNAUTHORIZED_DOCUMENT);
//                filterChain.doFilter(request, response);
//            }
            else if(StringUtil.matches(path, RegexMeta.ADMIN_PATH)) {
                if(session == null || session.getAttribute("user") == null) throw BaseException.of(ResponseMeta.UNAUTHORIZED_ADMIN);
                filterChain.doFilter(request, response);
            }
            else if(StringUtil.matches(path, SecurityMeta.DOCUMENT_LOGIN_URL)) {
                if(session != null && session.getAttribute("user") != null) throw BaseException.of(ResponseMeta.ALREADY_LOGIN_DOCUMENT_USER);
                filterChain.doFilter(request, response);
            }
            else if(StringUtil.matches(path, RegexMeta.RESOURCE_PATHS)
                    || StringUtil.matches(path, RegexMeta.LOGIN_PATH)
                    || StringUtil.matches(path, RegexMeta.EXTAPI_PATH)
                    || StringUtil.matches(path, RegexMeta.IMAGE_PATH)
                    || StringUtil.matches(path, RegexMeta.TOKEN_VALIDATE_PATH)
                    || StringUtil.matches(path, RegexMeta.DOCUMENT_PATHS)
            )
                filterChain.doFilter(request, response);
            else {
                String contentType = request.getContentType();

                HttpServletRequest req = contentType == null || contentType.equals(MediaType.APPLICATION_JSON_VALUE)
                        ? new GeneralHttpServletRequest(request, false) : (contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)
                        ? new MultipartHttpServletRequest(request, false) : request);

                HandlerMethod handlerMethod = (HandlerMethod)Objects.requireNonNull(handlerMapping
                                .getHandler(request))
                        .getHandler();

                if(handlerMethod.getMethodAnnotation(NoToken.class) != null)
                    filterChain.doFilter(req, response);
                else {
                    String token = request.getHeader("Authorization");

                    if(token == null) throw new JwtException("토큰 정보가 없음");

                    token = token.replace("Bearer ", "");

                    Boolean tokenValid = JwtUtil.validateToken(token);

                    log.debug("token:validate:" + tokenValid + ":" + JwtUtil.getUserKey(token) + ":" + JwtUtil.getUserId(token) + ":" + JwtUtil.getName(token));

                    if( JwtUtil.validateToken(token) ) {
                        //토큰 유효 하면 사용자 정보 Get

                        MemberDetail memberDetail = memberDetailService.findMemberByUserKey(JwtUtil.getUserKey(token));

                        String[] rolesArray = {};
                        List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();;

                        if(memberDetail.getRoles() != null && !memberDetail.getRoles().isBlank()) {
                            rolesArray = memberDetail.getRoles().split("\\|");
                            for (String role : rolesArray) {
                                roles.add(new SimpleGrantedAuthority(role));
                            }
                        }

                        if(req instanceof HttpServletRequestSupport) ((HttpServletRequestSupport)req).setRoles(roles);

                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                memberDetail, memberDetail.getPassword(), roles
                        );

                        SecurityContextHolder.getContext().setAuthentication(auth);
//                        SecurityContextHolder.getContext()
//                                .setAuthentication(new UsernamePasswordAuthenticationToken(UserInfo.builder()
//                                        .username( JwtUtil.getUserId(token) )
//                                        .name(JwtUtil.getName(token))
//                                        .password( password )
//                                        .authorities(roles)
//                                        .build(),
//                                        password,
//                                        roles));
                    }
                    else throw new JwtException("토큰 검증에 실패하였습니다.");

                    filterChain.doFilter(req, response);
                }
            }
        } catch(Exception e) {
            String msg;
            if(e instanceof BaseException) msg = ((BaseException)e).getInfo().getMessage();
            else msg = e.getMessage();
            authenticationEntryPoint.commence(request, response, new AuthenticationException(msg, e) {});
        }
    }
}
