package ai.maum.chathub.external.api.common;

import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.external.api.common.dto.req.ChatReq;
import ai.maum.chathub.external.api.common.service.CommonChatService;
import ai.maum.chathub.util.ObjectMapperUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/extapi/common")
public class ExternalController {
    private final CommonChatService commonChatService;

    @Operation(summary = "채팅", description = "채팅")
    @PostMapping({"/chat"})
    public BaseResponse<String> chat(
            @AuthenticationPrincipal UserDetails user,
//            @RequestParam(name="message", required=false) @Parameter(name = "채팅메시지") String message,
//            @RequestParam(name="chatbot_id", required=false) @Parameter(name = "chatbot_id") Long chatbotId,
//            @RequestBody(required = false) Map<String,Object> req
            @RequestBody(required = false) ChatReq chatReq
    ) {

//        String param1 = "";
//        Long param2 = 0L;
//
//        if(req != null) {
//            param1 = String.valueOf(req.get("message"));
//            param2 = Long.valueOf(String.valueOf(req.get("chatbot_id")));
//            log.debug("req: {}", ObjectMapperUtil.writeValueAsString(req));
//        }
//
        log.debug("message: {}", chatReq.getMessage());
        log.debug("chatbotId: {}", chatReq.getChatbotId());
        log.debug("param1: {}", chatReq.getUserId());

        String vendorId = user.getUsername();

        Boolean isAuth = commonChatService.checkAuthorize(vendorId, chatReq.getUserId(), chatReq.getChatbotId());

        return commonChatService.chat(chatReq.getMessage(), chatReq.getChatbotId(), chatReq.getUserId());
    }
}
