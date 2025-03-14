package ai.maum.chathub.conf.interceptor;

import ai.maum.chathub.meta.RegexMeta;
import ai.maum.chathub.util.NetworkUtil;
import ai.maum.chathub.util.ParsingUtil;
import ai.maum.chathub.util.StringUtil;
import ai.maum.chathub.api.execinfo.ExecInfoEntity;
import ai.maum.chathub.api.execinfo.ExecInfoRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API 수행 정보 인터셉터
 * @author baekgol
 */
public class ExecInfoInterceptor implements HandlerInterceptor {
    private final ExecInfoRepository execInfoRepository;

    public ExecInfoInterceptor(ExecInfoRepository execInfoRepository) {
        this.execInfoRepository = execInfoRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) {
        if(StringUtil.matches(request.getServletPath(), RegexMeta.RESPONSE_IGNORE_PATHS)) return;

        Map<String, Object> queryInfo = ParsingUtil.getQueryString(request);
        Object bodyInfo = request.getAttribute("args");

        if(bodyInfo != null) {
            if(bodyInfo instanceof List) bodyInfo = ((List<?>)bodyInfo).stream()
//                    .map(Object::toString)
                    .map(obj -> obj != null ? obj.toString() : "null") // null 예외 처리
                    .collect(Collectors.toList());
            else if(bodyInfo != null)
                bodyInfo = bodyInfo.toString();
        }

        long endTime = System.currentTimeMillis();

        ExecInfoEntity execInfo = ExecInfoEntity.builder()
                .result((String)request.getAttribute("result"))
                .api(request.getServletPath())
                .time(endTime - (long)request.getAttribute("startTime"))
                .ip(NetworkUtil.getIp(request))
                .build();

        Object paramInfo = null;

        if(queryInfo != null && bodyInfo != null) paramInfo = Map.of("query",
                queryInfo,
                "arg",
                bodyInfo);
        else if(queryInfo != null) paramInfo = queryInfo;
        else if(bodyInfo != null) paramInfo = bodyInfo;

        // FIXME mongodb to maria
        //if(paramInfo != null) execInfo.setParams(paramInfo);
        execInfoRepository.save(execInfo);
    }
}
