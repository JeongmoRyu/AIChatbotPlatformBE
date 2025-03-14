package ai.maum.chathub.api.file.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ChatbotFileSaveReq {
    @Schema(title="chatbot id", example = "1")
    private Integer chatbotId;
    @Schema(title="source file id", example = "[1,3,10]")
    private List<Integer> fileIds;
}
