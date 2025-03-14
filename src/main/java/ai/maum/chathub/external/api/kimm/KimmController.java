package ai.maum.chathub.external.api.kimm;

import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.external.api.kimm.service.KimmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name="한국기계원용API", description="한국기계원용API")
@RequestMapping("/kimm")
public class KimmController {
    private final KimmService kimmService;
    @Operation(summary = "로그인", description = "사용자 로그인")
    @ResponseBody
    @GetMapping({"/account/sync"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<Void> accountSync(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
    ) {
        log.info("KIMM API - {}", user.getUsername());
        kimmService.executeUserSync();
        return BaseResponse.success();
    }
}
