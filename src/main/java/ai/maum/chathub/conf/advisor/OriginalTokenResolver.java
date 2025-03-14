package ai.maum.chathub.conf.advisor;

import ai.maum.chathub.conf.annotation.OriginalToken;
import ai.maum.chathub.conf.security.HttpServletRequestSupport;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Objects;

/**
 * 원본 토큰 리졸버
 * @author baekgol
 */
public class OriginalTokenResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(String.class) && parameter.hasParameterAnnotation(OriginalToken.class);
    }

    @Override
    public Object resolveArgument(@NotNull MethodParameter parameter, ModelAndViewContainer mavContainer, @NotNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return Objects.requireNonNull(webRequest.getNativeRequest(HttpServletRequestSupport.class)).getOriginalToken();
    }
}
