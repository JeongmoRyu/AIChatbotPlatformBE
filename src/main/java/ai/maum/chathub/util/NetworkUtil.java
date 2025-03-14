package ai.maum.chathub.util;

import ai.maum.chathub.api.common.AmsBaseResponse;
import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.meta.ResponseMeta;
import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class NetworkUtil {
    private NetworkUtil() {}

    /**
     * 다른 서버와 HTTP 통신을 수행하는 RestTemplate 객체를 불러온다.
     * @return RestTemplate 객체
     * @author baekgol
     */
    public static RestTemplate getClient() {
        return new RestTemplate();
    }

    /**
     * 다른 서버와 HTTP 통신을 수행한다.
     * @param info 요청 정보
     * @param response 응답 클래스
     * @param errCode 오류 코드, Nullable
     * @return 응답 바디 객체
     * @throws BaseException 응답이 null 또는 통신 서버의 오류일 경우 예외
     * @author baekgol
     */
    @SuppressWarnings("unchecked")
    public static <T> T request(RequestInfo info, Class<T> response, @Nullable String errCode) {
        T res = request(info.url, info.method, response, info.contentType, info.body, info.headerParams, info.queryParams);

        if(res == null) throw BaseException.of(ResponseMeta.FAILURE);
        else if(res instanceof AmsBaseResponse && ((AmsBaseResponse<?>)res).getResult().equals("fail"))
            throw BaseException.of(errCode == null ? "ERROR" : errCode, ((AmsBaseResponse<?>)res).getMessage());
        else if(res instanceof Map && (int)((Map<String, Object>)res).get("code") == 202)
            throw BaseException.of((String)((Map<String, Object>)res).get("custom_code"), (String)((Map<String, Object>)res).get("body"));

        return res;
    }

    /**
     * 다른 여러 서버들과 HTTP 통신을 연속적으로 수행한다.
     * 요청한 통신 중 하나라도 실패할 경우 전체 실패 처리한다.
     * @param infos 요청 정보 목록
     * @param response 응답 클래스
     * @param errCode 오류 코드, Nullable
     * @return 응답 바디 객체 목록
     * @throws BaseException 응답이 null 또는 통신 서버의 오류일 경우 예외
     * @author baekgol
     */
    public static <T> List<Object> request(List<RequestInfo> infos, Class<T> response, @Nullable String errCode) {
        return infos.stream().map(info -> request(info, response, errCode)).collect(Collectors.toList());
    }

    /**
     * 요청 정보에서 클라이언트 IP를 불러온다.
     * @param request 요청 정보
     * @return 클라이언트 IP
     * @author baekgol
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if(isEmpty(ip)) ip = request.getHeader("Proxy-Client-IP");
        if(isEmpty(ip)) ip = request.getHeader("WL-Proxy-Client-IP");
        if(isEmpty(ip)) ip = request.getHeader("HTTP_CLIENT_IP");
        if(isEmpty(ip)) ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if(isEmpty(ip)) ip = request.getHeader("X-Real-IP");
        if(isEmpty(ip)) ip = request.getHeader("X-RealIP");
        if(isEmpty(ip)) ip = request.getHeader("REMOTE_ADDR");
        if(isEmpty(ip)) ip = request.getRemoteAddr();
        if(isEmpty(ip)) {
            try { ip = InetAddress.getLocalHost().toString().split("\\/")[1]; }
            catch (UnknownHostException ignored) {}
        }
        if(isEmpty(ip)) ip = "unknown";
        if(ip.contains(",")) {
            StringTokenizer st = new StringTokenizer(ip, ",");
            if(st.countTokens() > 0) ip = st.nextToken();
        }

        return ip;
    }

    /**
     * 다른 서버와 HTTP 통신을 수행한다.
     * 요청 헤더가 존재할 경우 Map<String, String> 객체를 파라미터로 넘겨준다.
     * 요청 쿼리가 존재할 경우 Map<String, Object> 객체를 파라미터로 넘겨준다.
     * 요청 바디가 존재할 경우 Object 객체를 파라미터로 넘겨준다.
     * @param url 요청 URL
     * @param method HTTP 메소드(GET, POST, PUT, PATCH, DELETE)
     * @param response 응답 클래스
     * @param contentType 데이터 형식
     * @param body 요청 바디 객체, Nullable
     * @param headerParams 요청 헤더 목록, Nullable
     * @param queryParams 요청 쿼리 파라미터 목록, Nullable
     * @return 응답 바디 객체, Nullable
     * @author baekgol
     */
    private static <T> @Nullable T request(String url,
                                           HttpMethod method,
                                           Class<T> response,
                                           @Nullable MediaType contentType,
                                           @Nullable Object body,
                                           @Nullable Map<String, String> headerParams,
                                           @Nullable Map<String, Object> queryParams) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType == null ? MediaType.APPLICATION_JSON : contentType);

        if(headerParams != null)
            for(String key: headerParams.keySet()) headers.add(key, headerParams.get(key));

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);

        if(queryParams != null)
            for(String key: queryParams.keySet()) uriBuilder.queryParam(key, queryParams.get(key));

        T res;
        MultiValueMap<String, Object> info = null;

        try {
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            if((contentType == MediaType.MULTIPART_FORM_DATA
                    || contentType == MediaType.APPLICATION_FORM_URLENCODED)
                    && body != null
                    && !(body instanceof MultiValueMap)) {
                info = ParsingUtil.parseObjectToMultiValueMap(body);
                entity = new HttpEntity<>(info, headers);
            }

            res = new RestTemplate().exchange(uriBuilder.toUriString(),
                            method,
                            entity,
                            response)
                    .getBody();
        } catch(Exception e) {
            deleteTempFiles(info);
            return null;
        }

        deleteTempFiles(info);

        return res;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteTempFiles(MultiValueMap<String, Object> info) {
        if(info != null) {
            info.forEach((k, v) -> {
                if(!v.isEmpty() && v.get(0) instanceof Resource) {
                    for(Object resource: v) {
                        try {
                            File file = ((Resource)resource).getFile();
                            file.delete();
                            file.getParentFile().delete();
                        } catch(IOException e) {
                            throw BaseException.of(e);
                        }
                    }
                }
            });
        }
    }

    private static boolean isEmpty(String ip) {
        return ip == null
                || ip.isEmpty()
                || ip.equalsIgnoreCase("unknown")
                || ip.equalsIgnoreCase("127.0.0.1")
                || ip.equalsIgnoreCase("localhost");
    }

    @Data
    @Builder
    public static class RequestInfo {
        private String url;
        private HttpMethod method;
        @Nullable
        private MediaType contentType;
        @Nullable
        private Object body;
        @Nullable
        private Map<String, String> headerParams;
        @Nullable
        private Map<String, Object> queryParams;
    }
}
