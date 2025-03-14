package ai.maum.chathub.conf.interceptor;

import ai.maum.chathub.util.LogUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExtApiInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LogUtil.debug("ExiApiInterceptor.preHandle");
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
