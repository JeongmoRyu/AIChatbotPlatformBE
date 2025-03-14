package ai.maum.chathub.api.common;

import ai.maum.chathub.meta.CodeInfo;
import ai.maum.chathub.util.LogUtil;
import ai.maum.chathub.util.StringUtil;
import lombok.Getter;

/**
 * 공통 예외
 * @author baekgol
 */
@Getter
public class BaseException extends RuntimeException {
    private CodeInfo info;

    private BaseException(String message) {
        super(message);
    }

    private BaseException(String code, String message) {
        super(message);
        this.info = new CodeInfo() {
            @Override
            public String getCode() {
                return code;
            }
            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    private BaseException(Exception e) {
        super(e);
    }

    private BaseException(CodeInfo info) {
        super(info.getMessage());
        this.info = info;
    }

    @Override
    public String toString() {
        Throwable e = getCause();
        String info = "예외";

        if(this.info != null) {
            info += ("(" + this.info.getCode());

            if(e != null) info += (", "
                    + e.getClass().getSimpleName()
                    + "): "
                    + StringUtil.getStackTrace(e));
            else info += ")";
        }
        else if(e != null) info += ("("
                + e.getClass().getSimpleName()
                + "): "
                + StringUtil.getStackTrace(e));
        else info += (": " + getMessage());

        return info;
    }

    /**
     * 공통 예외 객체를 반환한다.
     * 오류 메시지를 입력할 수 있다.
     * @param message 오류 메시지
     * @return 공통 예외 객체
     * @author baekgol
     */
    public static BaseException of(String message) {
        return new BaseException(message);
    }

    /**
     * 공통 예외 객체를 반환한다.
     * 오류 코드와 메시지를 입력할 수 있다.
     * @param code 오류 코드
     * @param message 오류 메시지
     * @return 공통 예외 객체
     * @author baekgol
     */
    public static BaseException of(String code, String message) {
        return new BaseException(code, message);
    }

    /**
     * 공통 예외 객체를 반환한다.
     * 예외 객체를 입력할 수 있다.
     * @param e 예외
     * @return 공통 예외 객체
     * @author baekgol
     */
    public static BaseException of(Exception e) {
        return new BaseException(e);
    }

    /**
     * 공통 예외 객체를 반환한다.
     * 오류 코드 및 메시지를 입력할 수 있다.
     * @param info 오류 정보
     * @return 공통 예외 객체
     * @author baekgol
     */
    public static BaseException of(CodeInfo info) {
        return new BaseException(info);
    }

    public static BaseException of(CodeInfo info, Exception e) {
        LogUtil.error(e);
        return of(info);
    }
}
