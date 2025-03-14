package ai.maum.chathub.api.chatbotInfo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatbotInfoOrganization {
    private Long chatbotId;
    private Long organizationId;
}
