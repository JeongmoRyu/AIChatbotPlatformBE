package ai.maum.chathub.api.agentchat.handler;

import ai.maum.chathub.api.agentchat.service.AgentChatService;
import ai.maum.chathub.mybatis.vo.ChatbotVO;
import ai.maum.chathub.mybatis.vo.ChatroomDetailVO;
import ai.maum.chathub.util.DateUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rag_service.rag_module.RagServiceGrpc;
import rag_service.rag_module.RagServiceGrpc.RagServiceStub;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class AgentChatGrpcConnectionHandler {

    private final AgentChatService agentChatService;

    @Value("${service.grpc.chat.host}")
    private String host;

    @Value("${service.grpc.chat.port}")
    private int port;

    @Getter
    @Setter
    public class ChatRequestObject {
        private ManagedChannel chnnal;
        private RagServiceStub stub;
        private AgentChatStreamObserverHandler streamObserver;
        private long creationTime;
        private long chatbotId;
        private String userKey;
        private long chatroomId;

        ChatRequestObject(ManagedChannel channel, RagServiceStub stub, AgentChatStreamObserverHandler streamObserver,
                          Long chatbotId, String userKey, Long chatroomId) {
            this.chnnal = channel;
            this.stub = stub;
            this.streamObserver = streamObserver;
            this.creationTime = System.currentTimeMillis();
            this.chatbotId = chatbotId;
            this.userKey = userKey;
            this.chatroomId = chatroomId;
        }

        ChatRequestObject() {

        }
    }

    private ConcurrentHashMap<String, ChatRequestObject> chatRequestMap = new ConcurrentHashMap<>();


    public ConcurrentHashMap<String, ChatRequestObject> getConnectionMaps() {
        return chatRequestMap;
    }
    @Scheduled(fixedRate = 600000) // 10분마다 실행
    public void cleanupExpiredObjects() {

        log.info("cleanupExpiredObjects execute!!!!");
        long currentTimeMillis = System.currentTimeMillis();
        long oneHourInMillis = TimeUnit.HOURS.toMillis(1);

        chatRequestMap.forEach((key, value) -> {
            long creationTime = value.getCreationTime(); // Assume you have a method to get the creation time
            log.info("check connection!!!!" + key + ":" + value.getCreationTime());
            if (currentTimeMillis - creationTime >= oneHourInMillis) {
                log.info("reset connection!!!!" + key);
                ManagedChannel channel = value.getChnnal();
//                if(!channel.isTerminated())
//                    channel.shutdown();
                shutdownChannel(channel);
                // Remove the expired object
                chatRequestMap.remove(key);
            }
        });
    }
    public int getConnectionSize() {
        return chatRequestMap.size();
    }

    public boolean connectionReset(Long chatroomId) {

        boolean rtn = false;

        ChatRequestObject chatRequestObject = chatRequestMap.get(String.valueOf(chatroomId));

        log.info("connection reset:" + chatroomId);

        if(chatRequestObject != null) {
            ManagedChannel channel = chatRequestObject.getChnnal();
//            if(!channel.isTerminated())
//                channel.shutdown();
            shutdownChannel(channel);
            chatRequestMap.remove(String.valueOf(chatroomId));
            rtn = true;
            log.info("connection reset OK:" + chatroomId);
        } else {
            log.info("connection reset FAIL not found:" + chatroomId);
        }

        return rtn;
    }
    public int connectionReset() {
        // chatRequestMap에서 모든 맵 항목을 가져와 특정 프로세스를 실행
        chatRequestMap.forEach((key, value) -> {
            log.info("reset Connection... " + key);
            ManagedChannel channel = value.getChnnal();
//            if(!channel.isTerminated())
//                channel.shutdown();
            shutdownChannel(channel);

            chatRequestMap.remove(key);
        });
        return chatRequestMap.size();
    }

    public Map<String, Object> getConnectoins() {
        Map<String, Object> returnMap = new HashMap<String,Object>();
        chatRequestMap.forEach( (key, value) -> {
            Map<String,Object> item = new HashMap<String,Object> ();
            item.put("chatbot_id", value.getChatbotId());
            item.put("creation_time", DateUtil.convertToStringByMs(value.getCreationTime()));
            item.put("user_key", value.getUserKey());
            item.put("chatroomId", value.getChatroomId());
            returnMap.put(key, item);
        });
        return returnMap;
    }

    public AgentChatStreamObserverHandler getStreamObserver(String roomId) {
        ChatRequestObject chatRequestObject = chatRequestMap.get(roomId);


        if(chatRequestObject != null) {
            //통신 가능한 상태인지 체크
            AgentChatStreamObserverHandler agentChatStreamObserverHandler = chatRequestObject.getStreamObserver();
            RagServiceStub stub = chatRequestObject.getStub();
            ManagedChannel channel = chatRequestObject.getChnnal();
            log.debug("channel shutdown:" + channel.isShutdown() + ":terminated:" + channel.isTerminated());
            if(!channel.isTerminated() && !channel.isShutdown() && stub != null && agentChatStreamObserverHandler != null) {
                log.debug("channel OK");
                return chatRequestObject.getStreamObserver();
            }
            else {
                //유효하지 않은 connection 제거
                log.debug("channel remove:" + roomId);
                chatRequestMap.remove(roomId);
                return null;
            }
        }
        else {
            log.debug("chatRequestObject is null:" + roomId);
            return null;
        }
//        return chatRequestMap.compute(roomId, k -> createStreamObserver());
    }

    public AgentChatStreamObserverHandler createStreamObserver(String roomId, ChatbotVO chatbot, ChatroomDetailVO chatroomDetail, String userKey) {
        ManagedChannel channel = createChannel();
        if(channel == null) {
            log.error("create GRPC Channel Create Error!!!");
            return null;
        }

        RagServiceStub ragServiceStub = createStub(channel);
        if(ragServiceStub == null) {
            log.error("create GRPC Stub Create Error!!!");
            return null;
        }

/*
        StreamObserver<ChatRequest> streamObserver = ragServiceStub.chatHandler(new StreamObserver<ChatResponse>() {
            @Override
            public void onNext(ChatResponse chatResponse) {
                // 서버로부터 응답을 받았을 때의 처리
                LogUtil.debug("protoTest Received response: " + chatResponse.getMsg());
                Status status = chatResponse.getStatus();
                LogUtil.debug("protoTest Received response: " + status != null ? status.getCodeValue() : "");
                try {
                    if (status != null && status.getCode() == Rag.Status.Code.STREAM_END) {
                        LogUtil.debug("protoTest Stream completed!!!(onNext)");
                        sink.complete();
                        chatService.sendSocketMessage(roomId, chatbot.getName(), "chat Complete(onNext)");
                    } else {
                        sink.next(chatResponse.getMsg());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable t) {
                // 에러 처리
                LogUtil.debug("protoTest error: " + t.getMessage());
//                    t.printStackTrace();
                sink.error(t);
                chatService.sendSocketMessage(roomId, chatbot.getName(), "chat Error:" + t.getMessage());
            }

            @Override
            public void onCompleted() {
                // 스트림이 완료되었을 때의 처리
                LogUtil.debug("proto Stream completed!!!");
                sink.complete();
//                channel.shutdownNow();
                chatService.sendSocketMessage(roomId, chatbot.getName(), "chat Complete");
                closeChannel(roomId);
            }
        });
*/

//        ChatStreamObserverHandler chatStreamObserverHandler = new ChatStreamObserverHandler(channel, ragServiceStub, chatScheduleManager, roomId, chatbot);
        AgentChatStreamObserverHandler agentChatStreamObserverHandler = new AgentChatStreamObserverHandler(channel, ragServiceStub, roomId, chatbot);
        ChatRequestObject chatRequestObject = new ChatRequestObject(channel, ragServiceStub, agentChatStreamObserverHandler, chatbot.getId(), userKey, chatroomDetail.getRoomId());
        chatRequestMap.put(roomId, chatRequestObject);

        return agentChatStreamObserverHandler;
    }

    private RagServiceStub createStub(ManagedChannel channel) {
//        return RagServiceGrpc.newStub(channel)
//                .withDeadlineAfter(120, TimeUnit.SECONDS);
        return RagServiceGrpc.newStub(channel);
    }

    private ManagedChannel createChannel() {
        log.debug("gRPC:createChannel!!!" + host + ":" + port);

        ManagedChannelBuilder channelBuilder = ManagedChannelBuilder.forAddress(host, port);

        if(port != 443)
            channelBuilder.usePlaintext();

//        return ManagedChannelBuilder.forAddress(host, port)
//                .usePlaintext()
//                .build();

        return channelBuilder.build();
    }

    public void closeChannel(String roomId) {
        ChatRequestObject chatRequestObject = chatRequestMap.get(roomId);
        if(chatRequestObject != null) {
            ManagedChannel channel = chatRequestObject.getChnnal();
            shutdownChannel(channel);
        }
        chatRequestMap.remove(roomId);
        /*
        ManagedChannel channel = chatRequestMap.get(roomId);
        if (channel != null) {
            channel.shutdown();
        }
        */
    }

    public void resetChannelByChatbotId(long chatbotId) {
        chatRequestMap.forEach((key, value) -> {
            log.info("check channel by ChatbotId:" + chatbotId + ":" + key + ":" + value.getChatbotId());
            if(value.getChatbotId() == chatbotId) {
                log.info("reset Channel by ChatbotId!!!!" + key + ":" + chatbotId);
                ManagedChannel channel = value.getChnnal();
//                if(!channel.isTerminated())
//                    channel.shutdown();
                shutdownChannel(channel);
                // Remove the expired object
                chatRequestMap.remove(key);
            }
        });
    }

    private void shutdownChannel(ManagedChannel channel) {
        channel.shutdown();
        try {
            log.info("shutdown channel - awaitTermination");
            if(!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                log.info("shutdown channel - shutdownnow");
                channel.shutdownNow();
                if(!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.info("channel did not terminate");
                }
            }
        } catch (InterruptedException e) {
            log.info("shutdown channel exception - shutdownnow");
            channel.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /*
    public void closeAllChannels() {
        channelMap.values().forEach(ManagedChannel::shutdown);
        channelMap.clear();
    }
    */

}
