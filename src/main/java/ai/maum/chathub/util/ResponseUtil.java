package ai.maum.chathub.util;

import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.meta.CodeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ResponseUtil {
    private static final ObjectMapper om = new ObjectMapper();

    private ResponseUtil() {}

    /**
     * 성공 응답을 작성한다.
     * 응답 코드 및 메시지가 기본값으로 입력된다.
     * @param response 응답 정보
     * @author baekgol
     */
    public static void success(HttpServletResponse response) throws IOException {
        setResponse(response, BaseResponse.success());
    }

    /**
     * 성공 응답을 작성한다.
     * 응답 데이터를 입력할 수 있다.
     * 응답 코드 및 메시지가 기본값으로 입력된다.
     * @param response 응답 정보
     * @param data 응답 데이터
     * @author baekgol
     */
    public static <T> void success(HttpServletResponse response, T data) throws IOException {
        setResponse(response, BaseResponse.success(data));
    }

    /**
     * 성공 응답을 작성한다.
     * 응답 코드와 메시지를 입력할 수 있다.
     * 응답 데이터가 필요하지 않을 경우 사용한다.
     * @param response 응답 정보
     * @param info 코드 정보
     * @author baekgol
     */
    public static void success(HttpServletResponse response, CodeInfo info) throws IOException {
        setResponse(response, BaseResponse.success(info));
    }

    /**
     * 성공 응답을 작성한다.
     * 응답 데이터와 메시지를 입력할 수 있다.
     * @param response 응답 정보
     * @param data 응답 데이터
     * @param message 응답 메시지
     * @author baekgol
     */
    public static <T> void success(HttpServletResponse response, T data, String message) throws IOException {
        setResponse(response, BaseResponse.success(data, message));
    }

    /**
     * 성공 응답을 작성한다.
     * 응답 데이터, 코드, 메시지를 입력할 수 있다.
     * @param response 응답 정보
     * @param data 응답 데이터
     * @param info 코드 정보
     * @author baekgol
     */
    public static <T> void success(HttpServletResponse response, T data, CodeInfo info) throws IOException {
        setResponse(response, BaseResponse.success(data, info));
    }

    /**
     * 성공 응답을 작성한다.
     * 응답 코드를 입력할 수 있다.
     * 응답 메시지에 발생한 예외 정보가 입력된다.
     * @param response 응답 정보
     * @param code 응답 코드
     * @param e 예외
     * @author baekgol
     */
    public static void success(HttpServletResponse response, String code, Exception e) throws IOException {
        setResponse(response, BaseResponse.success(code, e));
    }

    /**
     * 실패 응답을 작성한다.
     * 응답 메시지가 기본값으로 입력된다.
     * @param response 응답 정보
     * @author baekgol
     */
    public static void failure(HttpServletResponse response) throws IOException {
        setResponse(response, BaseResponse.failure());
    }

    /**
     * 실패 응답을 작성한다.
     * 응답 메시지를 입력할 수 있다.
     * @param response 응답 정보
     * @param message 응답 메시지
     * @author baekgol
     */
    public static void failure(HttpServletResponse response, String message) throws IOException {
        setResponse(response, BaseResponse.failure(message));
    }

    /**
     * 실패 응답을 작성한다.
     * 응답 메시지에 발생한 예외 정보가 입력된다.
     * @param response 응답 정보
     * @param e 예외
     * @author baekgol
     */
    public static void failure(HttpServletResponse response, Exception e) throws IOException {
        setResponse(response, BaseResponse.failure(e));
    }

    private static <T> void setResponse(HttpServletResponse response, BaseResponse<T> target) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());

        PrintWriter pw = response.getWriter();
        pw.print(om.writeValueAsString(target));
        pw.flush();
        pw.close();
    }
}
