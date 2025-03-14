package ai.maum.chathub.conf.advisor;

import ai.maum.chathub.conf.message.SlackMessenger;
import ai.maum.chathub.meta.LogLevelMeta;
import ai.maum.chathub.util.LogUtil;
import ai.maum.chathub.util.StringUtil;
import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.api.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 컨트롤러에서 발생한 예외에 대한 실패 응답 처리 핸들러
 * @author baekgol
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final SlackMessenger slackMessenger;

    @ExceptionHandler({Exception.class})
    public BaseResponse<Void> handle(Exception e) {
        if(!(e instanceof BaseException) || ((BaseException)e).getInfo() == null) {
            String message = StringUtil.getStackTrace(e);
            LogUtil.notice(message, LogLevelMeta.ERROR);
            slackMessenger.notice(message);
            return BaseResponse.failure(e);
        }

        return BaseResponse.success(((BaseException)e).getInfo());
    }
}
