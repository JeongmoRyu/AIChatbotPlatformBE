package ai.maum.chathub.conf.aspect;

import ai.maum.chathub.meta.RegexMeta;
import ai.maum.chathub.meta.SecurityMeta;
import ai.maum.chathub.util.NetworkUtil;
import ai.maum.chathub.util.ParsingUtil;
import ai.maum.chathub.util.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 요청 Aspect
 * @author baekgol
 */
@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class RequestAspect {
    private final HttpServletRequest request;
    private final ObjectMapper om;

    @Before("@annotation(org.springframework.web.bind.annotation.GetMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void requestGetOrDelete() {
        if(!StringUtil.matches(request.getServletPath(), SecurityMeta.DOCUMENT_LOGIN_URL)
                && !StringUtil.matches(request.getServletPath(), RegexMeta.DOCUMENT_PATHS))
            printInfo(ParsingUtil.getQueryString(request));
    }

    @Before("(@annotation(org.springframework.web.bind.annotation.PostMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PatchMapping)) " +
            "&& args(..)")
    public void requestPostOrPutOrPatch(JoinPoint joinPoint) throws IllegalAccessException, JsonProcessingException {
        if(StringUtil.matches(request.getServletPath(), SecurityMeta.DOCUMENT_LOGIN_URL)
                || StringUtil.matches(request.getServletPath(), RegexMeta.DOCUMENT_LOGIN_PATHS)) return;

        Object[] args = joinPoint.getArgs();

        Parameter[] params = ((MethodSignature)joinPoint.getSignature()).getMethod().getParameters();

        List<Object> ftArgs = new ArrayList<>();

        for(int i=0; i<params.length; i++) {
            if(!params[i].isAnnotationPresent(AuthenticationPrincipal.class))
                ftArgs.add(args[i]);
        }

        String contentType = request.getContentType();

        if(contentType != null && contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            if(ftArgs.size() > 0) {
                Object arg = ftArgs.get(0);

                for(Object ftArg: ftArgs) {
                    if(ftArg != null && ftArg.getClass().getPackageName().contains("ai.maum")) {
                        arg = ftArg;
                        break;
                    }
                }

//                for(Field field: arg.getClass().getDeclaredFields()) {
//                    field.setAccessible(true);
//
//                    if(field.get(arg) == null){
//                        String param = request.getParameter(field.getName());
//                        if(param == null || param.equals("")) continue;
//                        field.set(arg, field.getType() == List.class
//                                ? Arrays.asList(param.split(","))
//                                : om.readValue(om.writeValueAsString(param), field.getType()));
//                    }
//                }
                if(arg != null)
                    for (Field field : arg.getClass().getDeclaredFields()) {
                        // static 필드나 접근이 불가능한 필드는 무시
                        if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                            continue;
                        }


                        String fieldName = field.getName();
                        String methodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

                        try {
                            Method getter = arg.getClass().getMethod(methodName);
                            Object value = getter.invoke(arg);

                            if (value == null) {
                                String param = request.getParameter(field.getName());
                                if (param != null && !param.isEmpty()) {
                                    value = field.getType() == List.class
                                            ? Arrays.asList(param.split(","))
                                            : om.readValue(om.writeValueAsString(param), field.getType());

                                    String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                                    Method setter = arg.getClass().getMethod(setterName, field.getType());
                                    setter.invoke(arg, value);
                                }
                            }
                        } catch (NoSuchMethodException e) {
                            log.warn("No getter/setter for field: " + fieldName);
                        } catch (Exception e) {
                            log.error("Error processing field: " + fieldName + ": " + e.getMessage());
                        }

                        /*

                        // 필드 접근 가능 여부 확인
                        boolean isAccessible = field.canAccess(arg);

                        try {
                            // 필드가 접근 불가능한 경우 접근 가능하도록 설정
                            if (!isAccessible) {
                                field.setAccessible(true);
                            }

                            // 필드가 null인 경우에만 처리
                            if (field.get(arg) == null) {
                                String param = request.getParameter(field.getName());
                                if (param == null || param.isEmpty()) {
                                    continue;
                                }

                                Object value = field.getType() == List.class
                                        ? Arrays.asList(param.split(","))
                                        : om.readValue(om.writeValueAsString(param), field.getType());

                                field.set(arg, value);
                            }
                        } catch (InaccessibleObjectException e) {
                            // 필드 접근이 불가능한 경우 예외 처리
                            LogUtil.warn("Unable to access field: " + field.getName() + ":" + e.getMessage());
                        } catch (Exception e) {
                            // 기타 예외 처리
                            LogUtil.error("Error processing field: " + field.getName() + ":" +  e.getMessage());
                        } finally {
                            // 접근 권한을 원래 상태로 복원
                            if (!isAccessible) {
                                field.setAccessible(false);
                            }
                        }

                         */
                    }
            }
        }

        int size = ftArgs.size();
        Object argInfo = size > 1 ? ftArgs : (size == 1 ? ftArgs.get(0) : null);

        request.setAttribute("args", argInfo);
        printInfo(argInfo);
    }

    private void printInfo(Object data) {
        log.info("req,"
                + request.getServletPath()
                + ","
                + NetworkUtil.getIp(request)
                + (data != null ? ("," + data) : ""));
    }
}
