package ai.maum.chathub.util;

public class ThreadUtil {
    private ThreadUtil() {}

    /**
     * 스레드를 생성해 반환한다.
     * @param work 작업할 로직이 담긴 Runnable
     * @return 스레드
     * @author baekgol
     */
    public static Thread create(Runnable work) {
        return new Thread(work);
    }
}
