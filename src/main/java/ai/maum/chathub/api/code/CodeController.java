package ai.maum.chathub.api.code;

import ai.maum.chathub.api.code.dto.res.CodeRes;
import ai.maum.chathub.api.code.service.CodeService;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name="코드", description = "공통코드API")
@RequestMapping("/code")
@RequiredArgsConstructor
public class CodeController {
    private final CodeService codeService;

    @Operation(summary = "코드목록", description = "코드목록조회")
    @GetMapping("/{cdgroup_id}")
    public BaseResponse<List<CodeRes>> getCodeLIst(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "cdgroup_id", required=true) @Parameter(name = "코드ID", description = "코드ID") String cdgroupId
    ) {
        return BaseResponse.success(codeService.getCoeList(cdgroupId));
    }
}
