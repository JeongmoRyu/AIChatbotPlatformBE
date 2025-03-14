package ai.maum.chathub.api.engine;

import ai.maum.chathub.api.engine.entity.EngineEntity;
import ai.maum.chathub.api.engine.entity.LlmEngineEntity;
import ai.maum.chathub.api.engine.service.EngineService;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.engine.repo.LlmEngineRepository;
import ai.maum.chathub.api.engine.repo.RetrieveEngineRepository;
import ai.maum.chathub.api.member.service.MemberOrganizationService;
import ai.maum.chathub.api.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name="엔진", description="Retriever/LLM 엔진 관련 API")
public class EngineController {
    private final LlmEngineRepository llmEngineRepository;
    private final RetrieveEngineRepository retrieveEngineRepository;
    private final EngineService engineService;
    private final MemberOrganizationService memberOrganizationService;

    @Operation(summary = "Retrieve 목록 조회", description = "Retriever 엔진목록조회")
    @ResponseBody
    @GetMapping({"/engine", "/engine/{type}", "/engine/{type}/{vendor}"})
    public BaseResponse<List<EngineEntity>> getEngineList(
             @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
            ,@PathVariable(name = "type", required = false)  @Parameter(name = "type", description = "Vendor") String type
            ,@PathVariable(name = "vendor", required = false)  @Parameter(name = "vendor", description = "Vendor") String vendor
    ) {

        Long organizationId = memberOrganizationService.getMemberOrganizationId(user.getUserKey());

        return BaseResponse.success(engineService.getEngineList(type, vendor, false, organizationId));
    }

    @Operation(summary = "Retrieve 목록 조회", description = "Retriever 엔진목록조회")
    @ResponseBody
    @GetMapping({"/ragengine/{vendor}", "/ragengine"})
//    public BaseResponse<List<RetrieveEngineEntity>> getRetrieverList(
    public BaseResponse<List<EngineEntity>> getRetrieverList(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "vendor", required = false) @Parameter(name = "vendor", description = "Vendor") String vendor
    ) {
        return getEngineList(user, "RAG", vendor);
        /*
        List<RetrieveEngineEntity> retrieveEngineList = new ArrayList<RetrieveEngineEntity>();

        if(vendor == null || vendor.isEmpty()) {
            Sort sort = Sort.by(
                    new Sort.Order(Sort.Direction.ASC, "vendor"),
                    new Sort.Order(Sort.Direction.ASC, "seq")
            );
            retrieveEngineList = retrieveEngineRepository.findAll(sort);
        } else
            retrieveEngineList = retrieveEngineRepository.findByVendorOrderBySeqAsc(vendor);
        return BaseResponse.success(retrieveEngineList);
         */
    }

    @Operation(summary = "LLM 목록 조회", description = "LLM 엔진목록조회")
    @ResponseBody
    @GetMapping({ "/llmengine/{vendor}", "/llmengine"})
//    public BaseResponse<List<LlmEngineEntity>> getLLMList(
    public BaseResponse<List<EngineEntity>> getLLMList(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "vendor", required = false) @Parameter(name = "vendor", description = "Vendor") String vendor
    ) {
        return getEngineList(user, "LLM", vendor);

        /*
        List<LlmEngineEntity> retrieveEngineList = new ArrayList<LlmEngineEntity>();

        if(vendor == null || vendor.isEmpty()) {
            Sort sort = Sort.by(
                    new Sort.Order(Sort.Direction.ASC, "vendor"),
                    new Sort.Order(Sort.Direction.ASC, "seq")
            );

            retrieveEngineList = llmEngineRepository.findAll(Sort.by("vendor", "seq"));
        }
        else
            retrieveEngineList = llmEngineRepository.findLlmEngineEntitiesByVendorOrderBySeqAsc(vendor);
        return BaseResponse.success(retrieveEngineList);
         */
    }

    private LlmEngineEntity createNewEngine(
            String name) {
        LlmEngineEntity engine = new LlmEngineEntity();
        engine.setName(name);
        return engine;
    }
}
