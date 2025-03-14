package ai.maum.chathub.api.agentchat.handler;

import ai.maum.chathub.util.ObjectMapperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

@Slf4j
@Component
public class AgentFastApiWebSocketHandler extends TextWebSocketHandler {

    private final AgentChatSocketIOHandler agentChatSocketIOHandler;

    public AgentFastApiWebSocketHandler(AgentChatSocketIOHandler agentChatSocketIOHandler) {
        this.agentChatSocketIOHandler = agentChatSocketIOHandler;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Connected to FastAPI WebSocket");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("Message from FastAPI: " + message.getPayload());

        try {
            Map<String,Object>  resultMap = ObjectMapperUtil.readValue(message.getPayload(), Map.class);
            if(resultMap != null && resultMap.containsKey("client_id")) {
                String client_id = (String) resultMap.get("client_id");
                log.debug("client_id {}", client_id);
                agentChatSocketIOHandler.sendMessageToSession(client_id, "result", message.getPayload());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        // FastAPI에서 받은 메시지를 ChatSocketIOHandler로 전달
//        chatSocketIOHandler.broadcastToClients(message.getPayload());
    }
}
