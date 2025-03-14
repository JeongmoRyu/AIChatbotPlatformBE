package ai.maum.chathub.util;

import ai.maum.chathub.meta.LogLevelMeta;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogUtil {
    private LogUtil() {}

    /**
     * 로그 레벨 Info로 내용을 출력한다.
     * @param msg 출력할 내용
     * @author baekgol
     */
    public static void info(Object msg) {
        log.info(msg.toString());
    }

    /**
     * 로그 레벨 Warn으로 내용을 출력한다.
     * @param msg 출력할 내용
     * @author baekgol
     */
    public static void warn(Object msg) {
        log.warn(msg.toString());
    }

    /**
     * 로그 레벨 Error로 내용을 출력한다.
     * @param msg 출력할 내용
     * @author baekgol
     */
    public static void error(Object msg) {
        log.error(msg.toString());
    }

    /**
     * 예외에 대한 내용을 로그 레벨 Error로 출력한다.
     * @param e 예외
     * @author baekgol
     */
    public static void error(Exception e) {
        log.error(e.getMessage());
    }

    /**
     * 로그 레벨 Debug로 내용을 출력한다.
     * @param msg 출력할 내용
     * @author baekgol
     */
    public static void debug(Object msg) {
        log.debug(msg.toString());
    }

    /**
     * 로그 레벨 Trace로 내용을 출력한다.
     * @param msg 출력할 내용
     * @author baekgol
     */
    public static void trace(Object msg) {
        log.trace(msg.toString());
    }

    /**
     * 출력할 내용을 강조해야할 경우 구분선으로 감싸진 내용을 출력한다.
     * 출력할 내용을 작성하고 로그 레벨을 선택할수 있다.
     * @param msg 출력할 내용
     * @param level 로그 레벨
     * @author baekgol
     */
    public static void notice(Object msg, LogLevelMeta level) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("========================================================");
        sb.append("\n");
        sb.append(msg);
        sb.append("\n");
        sb.append("========================================================");

        if(level == LogLevelMeta.INFO) info(sb.toString());
        else if(level == LogLevelMeta.WARN) warn(sb.toString());
        else if(level == LogLevelMeta.ERROR) error(sb.toString());
        else if(level == LogLevelMeta.DEBUG) debug(sb.toString());
        else if(level == LogLevelMeta.TRACE) trace(sb.toString());
    }
}
