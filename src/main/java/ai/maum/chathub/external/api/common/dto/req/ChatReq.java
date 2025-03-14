package ai.maum.chathub.external.api.common.dto.req;

import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ChatReq {
    private String message;
    private Long chatbotId;
    private String userId;
}
