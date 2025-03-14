package ai.maum.chathub.api.nexon;

import ai.maum.chathub.api.nexon.service.NexonService;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.conf.message.SlackMessenger;
import ai.maum.chathub.meta.ResponseMeta;
import ai.maum.chathub.util.ObjectMapperUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name="외부API(넥슨)", description="외부 API(넥슨)")
@RequestMapping("/extapi/nexon")
public class NexonController {

    private final NexonService nexonService;
    private final SlackMessenger slackMessenger;

    @Operation(summary = "채팅", description = "채팅")
    @PostMapping({"/chat"})
    public BaseResponse<String> chat(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(name="message", required=false) @Parameter(name = "채팅메시지") String message,
            @RequestParam(name="chatbot_id", required=false) @Parameter(name = "chatbot_id") Long chatbotId,
            @RequestBody(required = false) Map<String,Object> req
    ) {

//        slackMessenger.notice("D066FGEQATD", "slack알림테스트");

        String param1 = "";
        Long param2 = 0L;

        if(req != null) {
            param1 = String.valueOf(req.get("message"));
            param2 = Long.valueOf(String.valueOf(req.get("chatbot_id")));
            log.debug("req: {}", ObjectMapperUtil.writeValueAsString(req));
        }

        log.debug("message: {}", message);
        log.debug("chatbotId: {}", chatbotId);
        log.debug("param1: {}", param1);
        log.debug("param2: {}", param2);

        if(param1 != null && !param1.isBlank() && param2 != null && param2 > 0L)
            return nexonService.nexonChat(param1, param2);
        else if(message != null && !message.isBlank() && chatbotId != null && chatbotId > 0L)
            return nexonService.nexonChat(message, chatbotId);
        else
            return BaseResponse.failure("", ResponseMeta.PARAM_WRONG);
    }
}
