package ai.maum.chathub.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.core.ApplicationPart;
import org.springframework.core.io.Resource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ParsingUtil {
    private static final ObjectMapper om = new ObjectMapper();

    private ParsingUtil() {}

    /**
     * 요청 정보에 있는 Query String 정보를 Map으로 반환한다.
     * @param request 요청 정보
     * @return Query String 정보가 담긴 Map, 존재하지 않을 경우 null
     * @author baekgol
     */
    public static Map<String, Object> getQueryString(HttpServletRequest request) {
        String qs = request.getQueryString();
        if(qs == null) return null;

        try {
            return Arrays.stream(URLDecoder.decode(qs, StandardCharsets.UTF_8).split("&"))
                    .map(param -> param.split("="))
                    .collect(Collectors.toMap(param -> param[0], param -> param[1]));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 요청 정보에 있는 Body 정보를 Map으로 반환한다.
     * &#064;RequestBody가 선언된 컨트롤러 메소드에는 사용할 수 없다.
     * @param request 요청 정보
     * @return Body 정보가 담긴 Map, 존재하지 않을 경우 null
     * @author baekgol
     */
    public static Map<String, Object> getBody(HttpServletRequest request) throws IOException {
        BufferedReader br = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) sb.append(line);
        return om.readValue(sb.toString(), new TypeReference<>() {});
    }

    /**
     * 해당 객체의 각 필드에 대한 값을 Map으로 반환한다.
     * @param object 객체 정보
     * @return 객체 정보가 담긴 Map
     * @author baekgol
     */
    public static Map<String, Object> parseObject(Object object) throws IllegalAccessException {
        Map<String, Object> info = new HashMap<>();
        Class<?> classInfo = object.getClass();

        if(!classInfo.getPackage().getName().startsWith("ai.maum")) return null;

        for(Field field: classInfo.getDeclaredFields()) {
            field.setAccessible(true);
            info.put(StringUtil.convertNaming(field.getName(), true), field.get(object));
        }

        return info;
    }

    /**
     * 해당 객체의 각 필드에 대한 값을 MultiValueMap으로 반환한다.
     * @param object 객체 정보
     * @return 객체 정보가 담긴 MultiValueMap
     * @author baekgol
     */
    @SuppressWarnings("unchecked")
    public static MultiValueMap<String, Object> parseObjectToMultiValueMap(Object object) throws IllegalAccessException, IOException {
        MultiValueMap<String, Object> info = new LinkedMultiValueMap<>();
        Class<?> classInfo = object.getClass();

        if(classInfo.getPackage().getName().startsWith("ai.maum")
                || (object instanceof MultipartFile)) {
            for(Field field: classInfo.getDeclaredFields()) {
                field.setAccessible(true);

                Object obj = field.get(object);
                String fieldName = StringUtil.convertNaming(field.getName(),true);

                if(obj instanceof Collection) {
                    for(Object e: (Collection<?>)obj) {
                        if(e instanceof MultipartFile)
                            info.add(fieldName, FileUtil.convertToResource((MultipartFile)e));
                        else if(e instanceof ApplicationPart) {
                            ApplicationPart part = (ApplicationPart)obj;
                            info.add(StringUtil.convertNaming(part.getName(), true), convertToResource(part));
                        }
                        else
                            info.add(fieldName, e);
                    }
                }
                else if(obj instanceof MultipartFile)
                    info.add(fieldName, FileUtil.convertToResource((MultipartFile)obj));
                else if(obj instanceof ApplicationPart) {
                    ApplicationPart part = (ApplicationPart)obj;
                    info.add(StringUtil.convertNaming(part.getName(), true), convertToResource(part));
                }
                else info.add(fieldName, obj);
            }
        }
        else if(object instanceof Map) ((Map<String, ?>) object).forEach(info::add);
        else return null;

        return info;
    }

    /**
     * Object 형식 그대로 형변환한다.
     * @param object 객체
     * @return object
     * @author baekgol
     */
    public static <T> T convertType(T object) {
        return object;
    }

    private static Resource convertToResource(ApplicationPart part) throws IOException {
        int idx = part.getSubmittedFileName().lastIndexOf("/");
        return FileUtil.convertToResource(idx == -1 ? part.getSubmittedFileName() : part.getSubmittedFileName().substring(idx + 1),
                part.getInputStream().readAllBytes());
    }
}
