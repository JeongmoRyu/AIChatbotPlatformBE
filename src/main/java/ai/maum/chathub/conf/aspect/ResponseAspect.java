package ai.maum.chathub.conf.aspect;

import ai.maum.chathub.meta.RegexMeta;
import ai.maum.chathub.util.LogUtil;
import ai.maum.chathub.util.NetworkUtil;
import ai.maum.chathub.util.StringUtil;
import ai.maum.chathub.api.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;

/**
 * 응답 Aspect
 * @author baekgol
 */
@Component
@Aspect
@RequiredArgsConstructor
public class ResponseAspect {
    private final HttpServletRequest request;

    @AfterReturning(
            pointcut = "@annotation(org.springframework.web.bind.annotation.GetMapping) " +
                    "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping) " +
                    "|| @annotation(org.springframework.web.bind.annotation.PostMapping) " +
                    "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
                    "|| @annotation(org.springframework.web.bind.annotation.PatchMapping)" +
                    "|| @annotation(org.springframework.web.bind.annotation.ExceptionHandler)",
            returning = "res"
    )
    public void response(Object res) {
        if(StringUtil.matches(request.getServletPath(), RegexMeta.DOCUMENT_LOGIN_PATHS)
                || StringUtil.matches(request.getServletPath(), RegexMeta.DOCUMENT_PATHS)
                || StringUtil.matches(request.getServletPath(), RegexMeta.RESPONSE_IGNORE_PATHS)) return;
        String result = "fail";
        if((res instanceof BaseResponse && ((BaseResponse<?>)res).getResult())
                || res instanceof SseEmitter) result = "ok";
        request.setAttribute("result", result);
        LogUtil.info("res,"
                + result
                + ","
                + (System.currentTimeMillis() - (long)request.getAttribute("startTime"))
                + "ms,"
                + request.getServletPath()
                + ","
                + NetworkUtil.getIp(request));
    }
}
