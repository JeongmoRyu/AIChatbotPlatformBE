package ai.maum.chathub.api.auth;

import ai.maum.chathub.api.auth.dto.LoginMember;
import ai.maum.chathub.api.auth.dto.LoginParam;
import ai.maum.chathub.api.auth.dto.req.ChangePasswordReq;
import ai.maum.chathub.api.auth.service.AuthService;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.mapper.MemberMapper;
import com.fasterxml.jackson.databind.ser.Serializers;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ai.maum.chathub.meta.ResponseMeta;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name="사용자계정", description="사용자계정관련API")
//@RequestMapping("/account")
public class UserInfoController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final MemberMapper memberMapper;

    @PutMapping("/account/change")
    public BaseResponse<MemberDetail> changeAccountInfo(
            @RequestHeader("Authorization") String authorizationHeader,
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @RequestBody MemberDetail memberDetail
    ) {
        //변경 가능한 DATA는 우선 이름만..
        MemberDetail modifyMemberDetail = new MemberDetail();

        modifyMemberDetail.setUserKey(user.getUserKey());
        modifyMemberDetail.setName(memberDetail.getName());

        return BaseResponse.success(authService.modifyMyAccount(modifyMemberDetail, authorizationHeader));
    }

        @PutMapping("/account/change/password")
    public BaseResponse<Void> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @RequestBody ChangePasswordReq changePasswordReq
    ) {

        String emailId = user.getUsername();
        LoginParam loginParam = new LoginParam(user.getUsername(), null, changePasswordReq.getPassword());

        try {
            LoginMember loginMember = authService.authLogin(loginParam);

            // accessToken이 null or blank일 경우 = 로그인 실패 = 패스워드 일치하지 않을때
            if(loginMember == null || loginMember.getAccessToken() == null || loginMember.getAccessToken().isBlank()) {
                return BaseResponse.failure(ResponseMeta.INVALID_PASSWORD);
            }

            Boolean bResult = authService.changePassword(user.getUserKey(), changePasswordReq.getNewPassword(), false, null);

            if(!bResult) {
                return BaseResponse.failure(ResponseMeta.FAILURE);
            }

        } catch (Exception e) {
            log.error("Failed to login {}", e.getMessage());
//            throw new RuntimeException(e);
        }
        //기존 패스워드 유효성 검사

        log.debug("{}", emailId);
        //패스워드 변경

        return BaseResponse.success();

    }
}
