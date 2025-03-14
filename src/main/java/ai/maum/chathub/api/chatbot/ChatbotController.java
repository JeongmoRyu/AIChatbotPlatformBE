package ai.maum.chathub.api.chatbot;

import ai.maum.chathub.api.chatbot.repo.ChatbotRepository;
import ai.maum.chathub.api.chatbot.service.ChatbotService;
import ai.maum.chathub.api.chatbot.dto.ChatbotContent;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.engine.repo.EngineRepository;
import ai.maum.chathub.api.member.dto.MemberDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@Tag(name="챗봇", description="챗봇관리API")
public class ChatbotController {

    private final ChatbotRepository chatbotRepository;
    private final ChatbotService chatbotService;
    private final EngineRepository engineRepository;

    // 조회는 모든 챗봇에 대해 가능.
//    @Deprecated
//    @Operation(summary = "조회", description = "전체챗봇목록/상세조회")
//    @ResponseBody
//    @GetMapping({"/chatbotall"})
//    public BaseResponse<List<ChatbotEntity>> getChatBotListAll(
//            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
//    ) {
//        String userId = user.getUsername();
//        List<ChatbotEntity> chatbotList = chatbotService.getChatbotList();
//        return BaseResponse.success(chatbotList);
//    }

//    @Deprecated
//    @Operation(summary = "조회", description = "내챗봇목록/상세조회")
//    @ResponseBody
//    @GetMapping({"/chatbot"})
//    public BaseResponse<List<ChatbotEntity>> getMyChatBotList(
//            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
//    ) {
//        String userId = user.getUsername();
//        return BaseResponse.success(chatbotService.getChatbotList(userId));
//    }

//    @Deprecated
//    @Operation(summary = "조회", description = "챗봇상세조회")
//    @ResponseBody
//    @GetMapping({"/chatbot/{chatbot_id}"})
//    public BaseResponse<List<ChatbotEntity>> getChatBot(
//            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
//            @PathVariable(name = "chatbot_id", required = true) @Parameter(name = "chatbot_id", description = "챗봇ID - 0 이면 전체") Long chatbotId
//    ) {
//        String userId = user.getUsername();
//        return BaseResponse.success(chatbotService.getChatbot(chatbotId));
//    }

//    @Deprecated
//    @Operation(summary = "생성", description = "새로운챗봇 생성")
//    @PostMapping("/chatbot")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
//    public Object registChatBot(
//            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
//            @RequestBody @Parameter(name = "챗봇상세", required = true) ChatbotEntity chatbot
//    ) {
//        String userId = user.getUsername();
//        chatbot.setUserId(userId);
//        chatbot.setId(0);
//
//        List<EngineParam> llmParam = chatbot.getLlmParameters();
//        List<EngineParam> ragParam = chatbot.getRagParameters();
//
//        if (llmParam == null || llmParam.size() < 1) {
//            if (chatbot.getLlmEngineId() != null && chatbot.getLlmEngineId() > 0) {
//                EngineEntity engine = engineRepository.getReferenceById(chatbot.getLlmEngineId());
//                if(engine != null)
//                    llmParam = engine.getParameters();
//                chatbot.setLlmParameters(llmParam);
//            }
//        }
//
//        if (ragParam == null || ragParam.size() < 1) {
//            if (chatbot.getRetrieverEngineId() != null && chatbot.getRetrieverEngineId() > 0) {
//                EngineEntity engine = engineRepository.getReferenceById(chatbot.getRetrieverEngineId());
//                if(engine != null)
//                    ragParam = engine.getParameters();
//                chatbot.setRagParameters(ragParam);
//            }
//        }
//
//        chatbot = chatbotRepository.save(chatbot);
//        return BaseResponse.success(chatbot);
//    }

//    @Deprecated
//    @Operation(summary = "수정", description = "기존 챗봇 수정")
//    @ResponseBody
//    @PutMapping("/chatbot/{chatbot_id}")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
//    public Object modifyChatBot(
//            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
//            @PathVariable(name = "chatbot_id", required = false) @Parameter(name = "chatbot_id", required = true) Long chatbotId,
//            @RequestBody @Parameter(name = "챗봇 상세 내용", required = true) ChatbotEntity chatbot
//    ) {
//        String userId = user.getUsername();
//        boolean isExist = chatbotRepository.existsByUserIdAndId(userId, chatbotId);
//        if (!isExist) {
//            return BaseException.of("존재하지 않는 chatbot 입니다.");
//        }
//
//        chatbot.setUserId(userId);
//        chatbot.setId(chatbotId);
//        chatbot = chatbotService.updateChatbot(chatbot);
////        chatbot = chatbotRepository.save(chatbot);
////        return BaseResponse.success(chatbot);
//
//        return BaseResponse.success(chatbot);
//    }

//    @Deprecated
//    @Operation(summary = "삭제", description = "챗봇 삭제")
//    @ResponseBody
//    @DeleteMapping("/chatbot/{chatbot_id}")
//    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
//    public Object removeChatBot(
//            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
//            @PathVariable(name = "chatbot_id", required = false) @Parameter(description = "삭제할 챗봇의 ID", required = true) Long chatbotId
//    ) {
//        return chatbotService.processChatbotRemoval(user.getUsername(), chatbotId);
//    }

    @Operation(summary = "챗봇 컨텐트 조회", description = "해당 챗봇의 컨텐트 (Title, Comment, Cards)를 조회한다.")
    @ResponseBody
    @GetMapping("/chatbot/contents")
    public BaseResponse<ChatbotContent> getChatbotContentAll (
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
    ) {
        return BaseResponse.success(chatbotService.getChatBotContent(0L));
    }

//    @Operation(summary = "챗봇 컨텐트 조회", description = "해당 챗봇의 컨텐트 (Title, Comment, Cards)를 조회한다.")
//    @ResponseBody
//    @GetMapping("/chatbot/contents/{chatbot_id}")
//    @Deprecated
//    public BaseResponse<ChatbotContent> getChatbotContent (
//            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
//            @PathVariable(name = "chatbot_id", required = true)  @Parameter(description = "챗봇 ID", required = true) Long chatbotId
//    ) {
//        return BaseResponse.success(chatbotService.getChatBotContent(chatbotId));
//    }
}