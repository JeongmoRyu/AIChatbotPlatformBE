package ai.maum.chathub.api.auth;

import ai.maum.chathub.api.auth.dto.LoginMember;
import ai.maum.chathub.api.auth.dto.LoginParam;
import ai.maum.chathub.api.auth.dto.req.PasswordResetReq;
import ai.maum.chathub.api.auth.dto.res.MemberForMemberList;
import ai.maum.chathub.api.auth.service.AuthService;
import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.service.MemberOrganizationService;
import ai.maum.chathub.api.member.service.MemberService;
import ai.maum.chathub.meta.ResponseMeta;
import ai.maum.chathub.util.AuthUtil;
import ai.maum.chathub.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name="인증", description="인증관련API")
@RequestMapping("/login")
public class AuthController {
//    private final MemberDetailService memberDetailService;
    private final AuthService authService;
    private final MemberService memberService;
    private final MemberOrganizationService memberOrganizationService;

    @Operation(summary = "로그인", description = "사용자 로그인")
    @ResponseBody
    @PostMapping({"/email4Sync"})
    public BaseResponse<LoginMember> authLogin(
            @RequestParam(name="user", required=false) String userType,
            @RequestBody LoginParam loginParam
    ) {
        log.debug("email:" + loginParam.getEmail());
        log.debug("password256:" + loginParam.getPassword256());
        log.debug("password512:" + loginParam.getPassword512());

        LoginMember loginMember = null;

        try {

            loginMember = authService.authLogin(loginParam);

            if (loginMember != null && loginMember.getAccessToken() != null && !loginMember.getAccessToken().isBlank()) {
                return BaseResponse.success(loginMember);
            } else
                return BaseResponse.failure(loginMember, "로그인실패");
        } catch (BaseException e) {
            return BaseResponse.failure(loginMember, e.getMessage());
        }
    }

    @Operation(summary = "계정등록(일괄)", description = "계정등록(일괄-엑셀파일)")
    @ResponseBody
    @PostMapping({"/registall"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<Void> registAccountFromFile(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @Parameter(description = "첨부파일(multi-part") @RequestPart("files") List<MultipartFile> files
//            @ModelAttribute("files") List<MultipartFile> files
    ) {

        if(files == null || files.isEmpty() || files.size() < 1)
            return BaseResponse.failure(null, ResponseMeta.PARAM_WRONG);

        return authService.createAccountFromFile(files, user);

//        return BaseResponse.success();
    }

    @Operation(summary = "계정등록", description = "계정등록(암호 평문으로 수신 - 테스트용")
    @ResponseBody
    @PostMapping({"/regist"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Deprecated
    public BaseResponse<MemberDetail> registAccountWithNoHash(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @RequestBody MemberDetail memberDetail
    ) {
        Boolean checkAuth = AuthUtil.checkAuthorize(user, memberDetail, true);

        if(!checkAuth)
           return BaseResponse.success(null, ResponseMeta.LACK_OF_AUTHORITY);
        else {
            Long organizationId = memberOrganizationService.getMemberOrganizationId(user.getUserKey());
            return BaseResponse.success(authService.createAccount(memberDetail, true, null, organizationId));
        }
    }

    @Operation(summary = "계정등록", description = "계정등록(암호 hash 값으로 수신 - 서비스용")
    @ResponseBody
    @PostMapping({"/account"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<MemberDetail> registAccountWithHash(
            @RequestHeader("Authorization") String authorizationHeader,
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @RequestBody MemberDetail memberDetail
    ) {

        Boolean checkAuth = AuthUtil.checkAuthorize(user, memberDetail, true);

        if(!checkAuth)
            return BaseResponse.success(null, ResponseMeta.LACK_OF_AUTHORITY);

        //이미 있는 ID 여부 체크
        MemberDetail checkMember = memberService.findMemberByUserId(memberDetail.getUsername());
        if(checkMember != null) {
            return BaseResponse.success(null, ResponseMeta.EXIST_USERID);
        }

        Boolean bHash = false;

        // 패스워드가 공란일때는 패스워드 = ID로 세팅 (초기 패스워드는 아이디)
        if(memberDetail.getPassword() == null || memberDetail.getPassword().isEmpty()) {
            memberDetail.setPassword(memberDetail.getUsername());
            bHash = true;
        }

        Long organizationId = memberOrganizationService.getMemberOrganizationId(user.getUserKey());
        return BaseResponse.success(authService.createAccount(memberDetail, bHash, authorizationHeader, organizationId));
    }

    @Operation(summary = "계정수정", description = "계정수정")
    @ResponseBody
    @PutMapping({"/account"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<MemberDetail> modifyAccount(
            @RequestHeader("Authorization") String authorizationHeader,
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @RequestBody MemberDetail memberDetail
    ) {

        MemberDetail resultMember = null;
        MemberDetail checkMember = memberService.findMemberByUserKey(memberDetail.getUserKey());

        if(checkMember == null)
            return BaseResponse.failure(resultMember, ResponseMeta.NOTEXIST_USERID);

        Boolean checkAuth = AuthUtil.checkAuthorize(user, checkMember, false);

        if(!checkAuth)
            if(!Objects.equals(user.getUserKey(), checkMember.getUserKey())) // 내 계정은 수정 가능.
                return BaseResponse.success(null, ResponseMeta.LACK_OF_AUTHORITY);

        //target 계정이 내 계정 보다 상위 계정이면 안됨.
        if(AuthUtil.getAuthCount(user) < AuthUtil.getAuthCount(memberDetail))
            return BaseResponse.success(null, ResponseMeta.LACK_OF_AUTHORITY);

        resultMember = authService.modefiyAccount(memberDetail, false, null);

        return BaseResponse.success(resultMember);
    }

    @Operation(summary = "계정목록조회(페이징)", description = "계정목록조회(페이징)")
    @ResponseBody
    @GetMapping({"/account/list-paged"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<Page<MemberForMemberList>> getMemberListPaged(
              @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
            , @Parameter(description = "조회할 page") @RequestParam(value="page", defaultValue = "0") int page
            , @Parameter(description = "size/page") @RequestParam(value="size", defaultValue = "10") int size
            , @Parameter(description = "검색어(아이디)") @RequestParam(value="email", required = false) String email
    ) {

        log.debug("USER_KEY: {}", user.getUserKey());

        return BaseResponse.success(authService.getMemberDetail(page, size, email, user));
    }

    @Operation(summary = "계정목록조회", description = "계정목록조회")
    @ResponseBody
    @GetMapping({"/account/list"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<List<MemberDetail>> getMemberList(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
    ) {
        return BaseResponse.success(authService.getMemberList());
    }

    @Operation(summary = "계정삭제", description = "계정삭제")
    @ResponseBody
    @DeleteMapping({"/account/{user_key}"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<Void> deleteMember(
            @RequestHeader("Authorization") String authorizationHeader,
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name="user_key", required=true) @Parameter(name = "user_key") Long userKey
    ) {
        //ID 존재 여부 체크
        MemberDetail checkMember = memberService.findMemberByUserKey(userKey);

        if(checkMember == null)
            return BaseResponse.failure(null, ResponseMeta.NOTEXIST_USERID);

        //본인 계정 여부 체크 (본인 계정 삭제 불가)
        if(Objects.equals(user.getUserKey(), userKey)) {
            return BaseResponse.failure(null, ResponseMeta.UNAUTHORIZED_DELETE_MYACCOUNT);
        }

        //권한 체크
        Boolean checkAuth = AuthUtil.checkAuthorize(user, checkMember, false);

        if(!checkAuth)
            return BaseResponse.success(null, ResponseMeta.LACK_OF_AUTHORITY);

        boolean result = authService.deleteMember(userKey, checkMember.getUsername(), authorizationHeader);
        if(result)
            return BaseResponse.success();
        else
            return BaseResponse.failure();
    }

    @Operation(summary = "계정삭제(다중)", description = "계정삭제(다중)")
    @ResponseBody
    @DeleteMapping({"/account/deleteall"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<Void> deleteMembers(
            @RequestHeader("Authorization") String authorizationHeader,
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @RequestBody List<Long> userKeyList
//            @PathVariable(name="user_key", required=true) @Parameter(name = "user_key") Long userKey
    ) {

        int succCnt = 0;
        int failCnt = 0;

        for(Long userKey : userKeyList) {

            //ID 존재 여부 체크
            MemberDetail checkMember = memberService.findMemberByUserKey(userKey);

            if(checkMember == null) {
                failCnt++;
                continue;
            }

            //본인 계정 여부 체크 (본인 계정 삭제 불가)
            if(Objects.equals(user.getUserKey(), userKey)) {
                failCnt++;
                continue;
            }

            // 권한 체크
            Boolean checkAuth = AuthUtil.checkAuthorize(user, checkMember, false);

            if(!checkAuth) {
                failCnt++;
                continue;
            }

            boolean result = authService.deleteMember(userKey, checkMember.getUsername(), authorizationHeader);
            if(result)
                succCnt++;
            else
                failCnt++;
        }

        String msg = "계정삭제: 성공-" + succCnt + "건, 실패-" + failCnt + "건";

        return BaseResponse.successWithMessage(ResponseMeta.SUCCESS, msg);

    }

    @Operation(summary = "토큰유효성체크", description = "토큰유효성체크")
    @ResponseBody
    @GetMapping({"/token/validate"})
    public BaseResponse<Void> checkTokenValidate(HttpServletRequest request
    ) {
        String token = request.getHeader("Authorization");
        if(token == null) throw new JwtException("토큰 정보가 없음");

        try {
            token = token.replace("Bearer ", "");
            Boolean tokenValid = JwtUtil.validateToken(token);
            if(tokenValid)
                return BaseResponse.success(ResponseMeta.TOKEN_VALIDATE);
            else
                return BaseResponse.success(ResponseMeta.TOKEN_INVALIDATE);
        } catch (Exception e) {
            return BaseResponse.success(ResponseMeta.TOKEN_INVALIDATE);
        }
    }

    @Operation(summary = "비밀번호 초기화", description = "비밀번호 초기화")
    @ResponseBody
    @PutMapping({"/account/resetpwd"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<Void> resetPassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @RequestBody PasswordResetReq passwordResetReq
    ) {

        if(passwordResetReq == null || passwordResetReq.getUserKey() == null) {
            return BaseResponse.failure(ResponseMeta.PARAM_WRONG);
        }

        MemberDetail  checkMember = memberService.findMemberByUserKey(passwordResetReq.getUserKey());

        if(checkMember == null) {
            return BaseResponse.failure(ResponseMeta.NOTEXIST_USERID);
        }

        Boolean checkAuth = AuthUtil.checkAuthorize(user, checkMember, false);

        if(!checkAuth) {
            return BaseResponse.success(ResponseMeta.UNAUTHORIZED_PERMISSION);
        }

        Boolean bResult = authService.changePassword(checkMember.getUserKey(), checkMember.getUsername(), true, authorizationHeader);

        if(bResult)
            return BaseResponse.success();
        else
            return BaseResponse.failure();
    }

    @Operation(summary = "계정 중복 체크", description = "계정 중복 체크")
    @ResponseBody
    @GetMapping({"/account/chkdup"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<Void> checkDuplicatedId(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @Parameter(name="email", description = "이메일(로그인아이디)") @RequestParam(name="email", required=true) String email
    ) {

        if(email == null || email.isEmpty()) {
            return BaseResponse.failure(ResponseMeta.PARAM_WRONG);
        }

        MemberDetail  memberDetail = memberService.findMemberByUserId(email);

        if(memberDetail == null) {
            return BaseResponse.success(ResponseMeta.NOTEXIST_USERID);
        } else {
            return BaseResponse.success(ResponseMeta.EXIST_USERID);
        }
    }
}
