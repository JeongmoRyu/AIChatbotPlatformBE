package ai.maum.chathub.api.agentchat.handler;

import ai.maum.chathub.util.LogUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AgentChatSocketHandler extends TextWebSocketHandler {
    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        LogUtil.info("WebSocket Connected:" + session.getId());
        sessions.put(session.getId(), session);
        Gson gson = new Gson();
        JsonObject rtnObject = new JsonObject();
        rtnObject.addProperty("room", session.getId());
        session.sendMessage(new TextMessage(new Gson().toJson(rtnObject)));
    }

    public void sendMessage(String roomId, String message) throws Exception {
        LogUtil.info("WebSocket Send Message:" + roomId);
        WebSocketSession session = sessions.get(roomId);
        if(session != null && session.isOpen()) {
            LogUtil.info("WebSocket Send Message Success:" + roomId);
            session.sendMessage(new TextMessage(message));
        } else {
            LogUtil.info("WebSocket Send Message Error:" + roomId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = "";
        if(session != null) {
            sessionId = session.getId();
        }
        super.afterConnectionClosed(session, status);
        if(session == null || !session.isOpen()) {
            sessions.remove(sessionId);
            LogUtil.info("WebSocket Closed:" + sessionId);
        } else {
            LogUtil.info("WebSocket Closed - session is null");
        }
    }

    //    @Override
//    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
//        super.handlePongMessage(session, message);
//    }
}
