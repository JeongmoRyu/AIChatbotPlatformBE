package ai.maum.chathub.conf.security;

import ai.maum.chathub.meta.ConstantsMeta;
import ai.maum.chathub.meta.RegexMeta;
import ai.maum.chathub.util.StringUtil;
import ai.maum.chathub.api.apiuser.model.ApiUser;
import ai.maum.chathub.api.apiuser.service.ApiUserService;
import ai.maum.chathub.api.member.service.MemberDetailService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class APIKeyAuthFilter extends OncePerRequestFilter {

    private ApiUserService apiUserService;
    private MemberDetailService memberDetailService;

    public APIKeyAuthFilter(ApiUserService apiUserService, MemberDetailService memberDetailService) {
        this.apiUserService = apiUserService;
        this.memberDetailService = memberDetailService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //미인증 URL 예외 처리
        String requestURI = request.getRequestURI();

        if(StringUtil.matches(requestURI, RegexMeta.EXTAPI_PATH)
                && !StringUtil.matches(requestURI, RegexMeta.KAKAO_SYNC_PATH)
        ) { //kakaoSync의 경우 헤더값 세팅이 불가능하여 예외 처리
            String apiKey = request.getHeader(ConstantsMeta.HEADER_NAME_API_KEY);
            String vendorId = request.getHeader(ConstantsMeta.HEADER_NAME_VENDOR_ID);

            if (apiKey == null || vendorId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("No API Key or No Vendor Id found in request headers");
                return;
            }

//            ApiUser apiUser = apiUserService.getApiUser(vendorId);
            ApiUser apiUser = new ApiUser();

            try {
                apiUser = apiUserService.getApiUser(vendorId);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }


//        //api key validate check
            if (!apiKey.equals(apiUser.getApiKey())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(vendorId + " api key is invalid");
                return;
            } else if (!"Y".equals(apiUser.getUseYn())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(vendorId + " is disabled");
                return;
            }

            //api validate check 이후 사용자 정보 Set
            //memberId와 VendorId를 이용해 userKey를 가져옴. (나중에 kakao이외의 다른 vendor 연동까지 고려
            String vendorType = "";
            switch(vendorId) {
                case("kakaobot"):
                    vendorType = "KAKAO";
                    break;
                case("facebook"):
                    vendorType = "FACEBOOK";
                    break;
                case("apple"):
                    vendorType = "APPLE";
                    break;
                case("telegram"):
                    vendorType = "TELEGRAM";
                    break;
                case("ap"):
                    vendorType = "AMORE";
                    break;
                default:
                    vendorType = "KAKAO";
                    break;
            }

            UserDetails userDetails = apiUserService.loadUserByUsername(vendorId);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    userDetails.getPassword(),
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        }

        filterChain.doFilter(request, response);
    }
}
