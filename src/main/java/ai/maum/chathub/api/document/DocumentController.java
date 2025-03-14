package ai.maum.chathub.api.document;

import ai.maum.chathub.conf.annotation.NoToken;
import ai.maum.chathub.meta.SecurityMeta;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.document.dto.req.DocumentLoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/doc")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    /**
     * API 문서 접근 인증을 위한 로그인 페이지를 반환한다.
     * @return API 문서 인증 로그인 페이지
     */
    @GetMapping("/login/page")
    @Operation(hidden = true,
            security = { @SecurityRequirement(name = SecurityMeta.DOCUMENT_NO_TOKEN) })
    @NoToken
    public String renderDocLoginPage(
            @RequestParam(name = "type", required = false) String type, Model model
    ) {
        model.addAttribute("type", type);
        return "doc_login";
    }

    /**
     * API 문서 접근 인증을 위한 로그인을 수행한다.
     * @param req 요청 정보
     */
    @PostMapping("/login")
    @Operation(hidden = true,
            security = { @SecurityRequirement(name = SecurityMeta.DOCUMENT_NO_TOKEN) })
    @ResponseBody
    @NoToken
    public BaseResponse<Void> loginDoc(@RequestBody DocumentLoginRequest req) {
        documentService.loginDoc(req);
        return BaseResponse.success();
    }
}
