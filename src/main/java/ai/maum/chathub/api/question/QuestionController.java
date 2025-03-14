package ai.maum.chathub.api.question;

import ai.maum.chathub.api.question.service.QuestionService;
import ai.maum.chathub.api.chatbot.entity.ChatbotEntity;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.question.dto.Question;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name="질문생성", description="질문생성API")
public class QuestionController {
private final QuestionService questionService;

    @Operation(summary = "질문생성", description = "챗봇 기본 질문 생성")
    @PostMapping("/chatbot/genquestion")
    public BaseResponse<List<Question>> generateQuestion(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @RequestBody @Parameter(name = "챗봇상세", required = true) ChatbotEntity chatbot
    ) {

        ;
        return BaseResponse.success(questionService.generateQuestion(chatbot));
    }

    @Operation(summary = "(대화기반)질문생성", description = "(대화기반)챗봇 기본 질문 생성")
    @PostMapping("/suggest/question/{chatbot_id}/{chatroom_id}/{seq}")
    public BaseResponse<List<Question>> suggestQuestion(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "chatbot_id", required = true) @Parameter(description = "챗봇ID", required = true) Long chatbotId,
            @PathVariable(name = "chatroom_id", required = true) @Parameter(description = "챗룸ID", required = true) Long chatroomId,
            @PathVariable(name = "seq", required = true) @Parameter(description = "seq", required = true) Long seq
    ) {
        return BaseResponse.success(questionService.suggestQuestion(chatbotId, chatroomId, seq));
    }
}
