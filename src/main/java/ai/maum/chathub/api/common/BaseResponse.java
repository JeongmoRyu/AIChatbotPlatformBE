package ai.maum.chathub.api.common;

import ai.maum.chathub.meta.CodeInfo;
import ai.maum.chathub.meta.ResponseMeta;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 공통 응답
 * @author baekgol
 */
@Data
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(title = "공통 응답", description = "기본적인 API에 적용될 공통 응답 양식이다.")
public class BaseResponse<T> {
    @Schema(title = "결과", description = "API 처리 결과 유무", example = "true")
    private Boolean result;
    @Schema(title = "코드", description = "API 처리 결과 코드", example = "F000")
    private String code;
    private T data;
    @Schema(title = "메시지", description = "API 처리 결과 메시지", example = "성공적으로 수행하였습니다.")
    private String message;

    private BaseResponse() {}

    /**
     * API 처리 결과가 성공일 경우 응답 양식이다.
     * 응답 코드 및 메시지가 기본값으로 입력된다.
     * @return 공통 응답 객체
     * @author baekgol
     */
    public static BaseResponse<Void> success() {
        BaseResponse<Void> res = new BaseResponse<>();
        res.result = true;
        res.code = ResponseMeta.SUCCESS.getCode();
        res.message = ResponseMeta.SUCCESS.getMessage();
        return res;
    }

    /**
     * API 처리 결과가 성공일 경우 응답 양식이다.
     * 응답 데이터를 입력할 수 있다.
     * 응답 코드 및 메시지가 기본값으로 입력된다.
     * @param data 응답 데이터
     * @return 공통 응답 객체
     * @author baekgol
     */
    public static <T> BaseResponse<T> success(T data) {
        BaseResponse<T> res = new BaseResponse<>();
        res.result = true;
        res.code = ResponseMeta.SUCCESS.getCode();
        res.data = data;
        res.message = ResponseMeta.SUCCESS.getMessage();

//        log.debug("BASERESPONSE PRN0:" + ObjectMapperUtil.writeValueAsString(data));
//        log.debug("BASERESPONSE PRN1:" + ObjectMapperUtil.writeValueAsString(res.data));
//        log.debug("BASERESPONSE PRN2:" + ObjectMapperUtil.writeValueAsString(res));

        return res;
    }

    /**
     * API 처리 결과가 성공일 경우 응답 양식이다.
     * 응답 코드와 메시지를 입력할 수 있다.
     * 응답 데이터가 필요하지 않을 경우 사용한다.
     * @param info 코드 정보
     * @return 공통 응답 객체
     * @author baekgol
     */
    public static BaseResponse<Void> success(CodeInfo info) {
        BaseResponse<Void> res = new BaseResponse<>();
        res.result = true;
        res.code = info.getCode();
        res.message = info.getMessage();
        return res;
    }

    /**
     * API 처리 결과가 성공일 경우 응답 양식이다.
     * 응답 데이터와 메시지를 입력할 수 있다.
     * @param data 응답 데이터
     * @param message 응답 메시지
     * @return 공통 응답 객체
     * @author baekgol
     */
    public static <T> BaseResponse<T> success(T data, String message) {
        BaseResponse<T> res = new BaseResponse<>();
        res.result = true;
        res.code = ResponseMeta.SUCCESS.getCode();
        res.data = data;
        res.message = message;
        return res;
    }

    /**
     * API 처리 결과가 성공일 경우 응답 양식이다.
     * 응답 데이터, 코드, 메시지를 입력할 수 있다.
     * @param data 응답 데이터
     * @param info 코드 정보
     * @return 공통 응답 객체
     * @author baekgol
     */
    public static <T> BaseResponse<T> success(T data, CodeInfo info) {
        BaseResponse<T> res = new BaseResponse<>();
        res.result = true;
        res.code = info.getCode();
        res.data = data;
        res.message = info.getMessage();
        return res;
    }

    /**
     * API 처리 결과가 성공일 경우 응답 양식이다.
     * 응답 코드를 입력할 수 있다.
     * 응답 메시지에 발생한 예외 정보가 입력된다.
     * @param code 응답 코드
     * @param e 예외
     * @return 공통 응답 객체
     * @author baekgol
     */
    public static BaseResponse<Void> success(String code, Exception e) {
        BaseResponse<Void> res = new BaseResponse<>();
        res.result = true;
        res.code = code;
        res.message = e.getMessage();
        return res;
    }

    public static BaseResponse<Void> successWithMessage(CodeInfo info, String msg) {
        BaseResponse<Void> res = new BaseResponse<>();
        res.result = true;
        res.code = info.getCode();
        res.message = msg;
        return res;
    }


    /**
     * API 처리 결과가 실패일 경우 응답 양식이다.
     * 응답 메시지가 기본값으로 입력된다.
     * @return 공통 응답 객체
     * @author baekgol
     */
    public static BaseResponse<Void> failure() {
        BaseResponse<Void> res = new BaseResponse<>();
        res.result = false;
        res.message = ResponseMeta.FAILURE.getMessage();
        return res;
    }

    /**
     * API 처리 결과가 실패일 경우 응답 양식이다.
     * 응답 메시지를 입력할 수 있다.
     * @param message 응답 메시지
     * @return 공통 응답 객체
     * @author baekgol
     */
    public static BaseResponse<Void> failure(String message) {
        BaseResponse<Void> res = new BaseResponse<>();
        res.result = false;
        res.message = message;
        return res;
    }

    /**
     * API 처리 결과가 실패일 경우 응답 양식이다.
     * 응답 메시지에 발생한 예외 정보가 입력된다.
     * @param e 예외
     * @return 공통 응답 객체
     * @author baekgol
     */
    public static BaseResponse<Void> failure(Exception e) {
        BaseResponse<Void> res = new BaseResponse<>();
        res.result = false;
        res.message = e.getMessage();
        return res;
    }

    public static <T> BaseResponse<T> failure(T data, String message) {
        BaseResponse<T> res = new BaseResponse<>();
        res.result = false;
        res.code = ResponseMeta.FAILURE.getCode();
        res.data = data;
        res.message = message;
        return res;
    }

    public static BaseResponse<Void> failure(CodeInfo info, String message) {
        BaseResponse<Void> res = new BaseResponse<>();
        res.result = false;
        res.code = info.getCode();
        res.message = info.getMessage() + "\n" + message;
        return res;
    }

    public static BaseResponse<Void> failure(CodeInfo info) {
        BaseResponse<Void> res = new BaseResponse<>();
        res.result = false;
        res.code = info.getCode();
        res.message = info.getMessage();
        return res;
    }

    public static <T> BaseResponse<T> failure(T data, CodeInfo info) {
        BaseResponse<T> res = new BaseResponse<>();
        res.result = false;
        res.code = info.getCode();
        res.message = info.getMessage();
        return res;
    }

    public static BaseResponse<Void> failure(CodeInfo info, Exception e) {
        BaseResponse<Void> res = new BaseResponse<>();
        res.result = false;
        res.code = info.getCode();
        res.message = info.getMessage() + ":" + e.getMessage();
        return res;
    }
}
