package ai.maum.chathub.api.agentchat.handler;

import ai.maum.chathub.api.agentchat.service.AgentOpenAIChatService;
import ai.maum.chathub.api.chatplay.dto.req.LlmRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.protocol.PacketType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentChatSocketIOHandler {

    @Value("${service.ranker-api.ip}")
    private String RANKER_IP;
    @Value("${service.ranker-api.port}")
    private String RANKER_PORT;


    private final SocketIOServer server;
    private final AgentOpenAIChatService agentOpenAIChatService;

//    private Map<String, SocketIOClient> sessions = new ConcurrentHashMap<>();
    private Map<String, Object> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService pingScheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        try {
            // 기본 네임스페이스 ("/") 처리
            server.addConnectListener(client -> {
                log.info("socket init {}, {}, {}, {}", server.getConfiguration().getPingInterval(), server.getConfiguration().getPingTimeout(), PacketType.valueOf("PING"), PacketType.PING);

                Packet packet = new Packet(PacketType.PING);
                client.send(packet);

                String isRanker = client.getHandshakeData().getSingleUrlParam("is_ranker");
                String sessionId = null;

                try {
                    if ("true".equalsIgnoreCase(isRanker)) {
                        sessionId = connectToFastApi();
                        sessions.put(sessionId, client);  // FastAPI client_id로 저장
                    } else {
                        sessionId = UUID.randomUUID().toString();
                        sessions.put(sessionId, client);
                    }

                    log.info("socket.io Connected: {}, is_ranker: {}", sessionId, isRanker);

                    Map<String, Object> rtnObject = new HashMap<>();
                    rtnObject.put("room", sessionId);
                    client.sendEvent("message", rtnObject);
                    startPing(client);
                } catch (Exception e) {
                    log.error("Error during connection setup: {}", e.getMessage());
                    if (sessionId != null) {
                        sessions.remove(sessionId);
                    }
                    client.disconnect();
                }
            });

            // "/llm" 네임스페이스 처리
            SocketIONamespace llmNamespace = server.addNamespace("/llm");

            llmNamespace.addConnectListener(client -> {
                log.info("Connected to /llm namespace: {}", client.getSessionId());
            });

            llmNamespace.addDisconnectListener(client -> {
                log.info("Disconnected from /llm namespace: {}", client.getSessionId());
            });

            // llm_request 메시지 처리
            llmNamespace.addEventListener("llm_request", LlmRequest.class, (client, data, ackSender) -> {
                log.info("Language: {}", data.getLanguage());
                log.info("Prompt: {}", data.getPrompt());

                // LLM 요청 처리 로직
                try {
                    processLLMRequest(client, data);
//                    String result = processLLMRequest(client, data);
//                    log.info("Processed LLM request: {}", result);

                    // 처리 결과를 클라이언트로 전송
//                    client.sendEvent("llm_response", result);
                } catch (Exception e) {
                    log.error("Error processing llm_request: {}", e.getMessage());
                    client.sendEvent("llm_error", "Failed to process request");
                }
            });

            // 기본 메시지 처리 ("/" 네임스페이스)
            server.addEventListener("message", String.class, (client, data, ackSender) -> {
                log.debug("message received from client: " + data);
            });

            server.addEventListener("data", String.class, (client, data, ackSender) -> {
                log.debug("data received from client: " + data);
            });

            server.addEventListener("chat", String.class, (client, data, ackSender) -> {
                log.debug("chat received from client: " + data);
            });

            server.addDisconnectListener(client -> {
                String sessionId = client.getSessionId().toString();
                sessions.remove(sessionId);
                log.info("Disconnected: {}", sessionId);
            });

            server.start();
        } catch (Exception e) {
            log.error("Chat socket init error: {}", e.getMessage());
        }
    }

    private void processLLMRequest(SocketIOClient client, LlmRequest request) {
        // LLM 요청 처리 로직 구현
        log.info("Processing LLM request data: {}", request);

        // 예제: 데이터를 가공하거나 외부 API 호출 등 수행
        agentOpenAIChatService.processOpenAIStream(request, client);

//        String result = "Processed: Language=" + request.getLanguage() + ", Prompt=" + request.getPrompt();

//        return result;
    }

    private String connectToFastApi() {
        StandardWebSocketClient client = new StandardWebSocketClient();
        AgentFastApiWebSocketHandler handler = new AgentFastApiWebSocketHandler(this);

        try {
            String clientId = UUID.randomUUID().toString();
            // FastAPI와 WebSocket 연결 및 client_id 반환
            WebSocketSession session = client.doHandshake(handler, String.valueOf(URI.create("ws://" + RANKER_IP + ":" + RANKER_PORT + "/ws?client_id=" + clientId))).get();
//            String clientId = session.getId();
            sessions.put(clientId, session);  // FastAPI 세션을 sessions 맵에 저장
            log.info("Connected to FastAPI with client ID: {}", clientId);
            return clientId;
        } catch (Exception e) {
            log.error("Failed to connect to FastAPI: {}", e.getMessage());
            return null;
        }
    }

    private void startPing(SocketIOClient client) {
        // 30초 간격으로 Ping 메시지 전송
        pingScheduler.scheduleAtFixedRate(() -> {
            try {
                if (client.isChannelOpen()) {
//                    client.sendEvent("ping", "ping");
                    log.info("Ping message sent to client: {}", client.getSessionId());

                    Packet packet = new Packet(PacketType.PING);
                    client.send(packet);
                }
            } catch (Exception e) {
                log.error("Error sending ping message: {}", e.getMessage());
            }
        }, 0, 20, TimeUnit.SECONDS);
    }

    public void broadcastToClients(String message) {
        sessions.values().forEach(session -> {
            if (session instanceof SocketIOClient) {
                ((SocketIOClient) session).sendEvent("chat", message);
            } else if (session instanceof WebSocketSession) {
                try {
                    ((WebSocketSession) session).sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    log.error("Error broadcasting to FastAPI client: {}", e.getMessage());
                }
            }
        });
        log.info("Broadcasted message to all clients: {}", message);
    }

    public void sendMessageToSession(String sessionId, String title, String content) {
        // sessionId를 UUID로 변환
        UUID sessionUUID = UUID.fromString(sessionId);

        // 해당 sessionId에 해당하는 SocketIOClient 찾기
        SocketIOClient client = (SocketIOClient) sessions.get(sessionUUID.toString());

        if (client != null) {
            log.info("socket.io send message!!!:" + sessionId);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode messageObject = mapper.createObjectNode();
            messageObject.put("title", title);
            messageObject.put("content", content);
            client.sendEvent("chat", messageObject);
        } else {
            log.info("socket.io send message - session is null!!!:" + sessionId);
        }
    }


}
