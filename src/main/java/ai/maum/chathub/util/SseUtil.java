package ai.maum.chathub.util;

import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.meta.ResponseMeta;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SseUtil {
    private static final long timeout = 1000L * 60 * 5;
    private static final Map<String, EmitterInfo> emitters = new ConcurrentHashMap<>();

    /**
     * 주어진 ID를 통해 SSE Emitter를 생성한다.
     * @param id ID
     * @author baekgol
     */
    public static void create(String id) {
        if(emitters.containsKey(id)) throw BaseException.of(ResponseMeta.SSE_EMITTER_ID_ALREADY_EXIST);

        SseEmitter emitter = new SseEmitter(timeout);
        emitter.onCompletion(() -> remove(id));
        emitter.onTimeout(() -> remove(id));
        emitter.onError(error -> remove(id));

        emitters.put(id, EmitterInfo.builder()
                .id(id)
                .emitter(emitter)
                .subscribers(new HashSet<>())
                .build());
    }

    /**
     * 특정 클라이언트와 SSE 연결을 구독한다.
     * @param id ID
     * @param clientId 클라이언트 ID
     * @return SSE Emitter 객체
     * @author baekgol
     */
    public static SseEmitter createAndSubscribe(String id, String clientId) {
        try {
            create(id);
        } catch(BaseException e) {
            if(!e.getInfo().equals(ResponseMeta.SSE_EMITTER_ID_ALREADY_EXIST))
                throw e;
        }

        return subscribe(id, clientId);
    }

    /**
     * SSE Emitter를 조회한다.
     * @param id ID
     * @return SSE Emitter 정보
     * @author baekgol
     */
    public static EmitterInfo find(String id) {
        EmitterInfo info = emitters.get(id);
        if(info == null) throw BaseException.of(ResponseMeta.SSE_EMITTER_NOT_EXIST);
        return info;
    }

    /**
     * SSE Emitter 목록을 조회한다.
     * @return SSE Emitter 정보 목록
     * @author baekgol
     */
    public static List<EmitterInfo> findAll() {
        return new ArrayList<>(emitters.values());
    }

    /**
     * SSE Emitter를 삭제한다.
     * 해당 ID의 모든 연결들이 끊긴다.
     * @param id ID
     * @author baekgol
     */
    public static void delete(String id) {
        EmitterInfo info = emitters.get(id);
        if(info == null) throw BaseException.of(ResponseMeta.SSE_EMITTER_NOT_EXIST);
        info.emitter.complete();
    }

    /**
     * SSE 연결로 해당 ID에 연결된 모든 클라이언트들에게 메시지를 전송한다.
     * @param id ID
     * @param message 메시지
     * @author baekgol
     */
    public static void send(String id, String message) throws IOException {
        EmitterInfo info = emitters.get(id);
        if(info == null) throw BaseException.of(ResponseMeta.SSE_EMITTER_NOT_EXIST);
        info.emitter.send(message);
    }

    /**
     * 특정 클라이언트와 SSE 연결을 구독한다.
     * @param id ID
     * @param clientId 클라이언트 ID
     * @return SSE Emitter 객체
     * @author baekgol
     */
    public static SseEmitter subscribe(String id, String clientId) {
        EmitterInfo info = emitters.get(id);
        if(info == null) throw BaseException.of(ResponseMeta.SSE_EMITTER_NOT_EXIST);
        if(info.subscribers.stream().anyMatch(subscriber -> subscriber.equals(clientId))) throw BaseException.of(ResponseMeta.SSE_EMITTER_CLIENT_ID_ALREADY_EXIST);
        info.subscribers.add(clientId);
        return info.emitter;
    }

    /**
     * 특정 클라이언트의 SSE 연결을 종료한다.
     * @param id ID
     * @param clientId 클라이언트 ID
     * @author baekgol
     */
    public static void unsubscribe(String id, String clientId) {
        EmitterInfo info = emitters.get(id);
        if(info == null) throw BaseException.of(ResponseMeta.SSE_EMITTER_NOT_EXIST);
        info.subscribers.removeIf(subscriber -> subscriber.equals(clientId));
    }

    private static void remove(String id) {
        emitters.remove(id);
        LogUtil.info("SSE Emitter 삭제 - " + id);
    }

    @Getter
    @ToString
    @Builder
    public static class EmitterInfo {
        private String id;
        private SseEmitter emitter;
        private Set<String> subscribers;
    }
}
