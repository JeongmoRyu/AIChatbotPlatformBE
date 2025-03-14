package ai.maum.chathub.external.api.n8n.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name="local_agent용 임시 컨트롤러", description="local_agent용 임시 컨트롤러")
@RequestMapping("/n8n")
public class N8nController {
    private final N8nService n8nService;

    @PostMapping("/account/sync")
    @Operation(summary = "chathub사용자계정->local_agent에 생성")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
    public void syncAccount() {

        n8nService.syncAccount();
        // TODO : n8n sync account logic here

        log.info("n8n sync account");
    }
}
