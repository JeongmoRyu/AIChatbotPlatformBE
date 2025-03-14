package ai.maum.chathub.api.agentchat.handler;

import ai.maum.chathub.api.agentchat.service.AgentChatService;
import ai.maum.chathub.mybatis.vo.ChatbotVO;
import ai.maum.chathub.mybatis.vo.ChatroomDetailVO;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import rag_service.rag_module.Rag;
import rag_service.rag_module.Rag.ChatRequest;
import rag_service.rag_module.RagServiceGrpc;
import reactor.core.publisher.FluxSink;

@Slf4j
@Getter
@Setter
public class AgentChatStreamObserverHandler implements StreamObserver<ChatRequest> {
    private final StreamObserver<ChatRequest> baseStreamObserver;
    private FluxSink<String> sink;
    private AgentChatService agentChatService;
    private ChatroomDetailVO chatroomDetail;
    private ChatbotVO chatbot;
    private String question;
    private String memberId;
    private AgentChatScheduleTask agentChatScheduleTask;
    private AgentChatGrpcConnectionHandler connectionHandler;
    private final String errMessage = "오류가 발생 하였습니다. 잠시후 다시 시도해 주세요.";

//    public void setBaseStreamObserver(FluxSink<String> sink, ChatService chatService, ChatroomDetailVO chatroomDetail, ScheduledExecutorService scheduler) {
//    public void setBaseStreamObserver(FluxSink<String> sink, ChatService chatService, ChatroomDetailVO chatroomDetail, String memberId, ChatbotVO chatbot, String question) {
    public void setBaseStreamObserver(AgentChatGrpcConnectionHandler connectionHandler, FluxSink<String> sink, AgentChatService agentChatService, ChatroomDetailVO chatroomDetail, AgentChatScheduleTask task, String userQuestion) {
        setConnectionHandler(connectionHandler);
        setFluxSink(sink);
        setAgentChatService(agentChatService);
        setChatroomDetail(chatroomDetail);
        setAgentChatScheduleTask(task);
//        setChatScheduleManager(chatScheduleManager);
//        setChatbot(chatbot);
        setQuestion(userQuestion);
//        setScheduler(scheduler);
    }

    private void setQuestion(String question) {
        this.question = question;
    }

    //    public void setChatService(ChatService chatService) {
//        this.chatService = chatService;
//    }

    public void setConnectionHandler(AgentChatGrpcConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }
    public void setFluxSink(FluxSink<String> sink) {
        this.sink = sink;
    }
//    public void setChatroomDetail(ChatroomDetailVO chatroomDetail) { this.chatroomDetail = chatroomDetail; }
//    public void setScheduler(ScheduledExecutorService scheduler) { this.scheduler = scheduler; }

    public AgentChatStreamObserverHandler(StreamObserver<ChatRequest> baseStreamObserver) {
        this.baseStreamObserver = baseStreamObserver;
    }

    public AgentChatStreamObserverHandler(ManagedChannel channel, RagServiceGrpc.RagServiceStub ragServiceStub, String roomId, ChatbotVO chatbot) {
//        this.chatScheduleManager = new ChatScheduleManager(0, maxExecutionTime * 1000);
        this.baseStreamObserver = ragServiceStub.chatHandler(new StreamObserver<Rag.ChatResponse>() {
            StringBuffer stringBuffer = new StringBuffer();
            @Override
            public void onNext(Rag.ChatResponse chatResponse) {
                log.info("GRPC Received response(chatHandler.onNext): " + chatResponse.getMsg() + ":" + chatbot.getId() + ":" + question + ":" + sink.isCancelled());
                stringBuffer.append(chatResponse.getMsg());
                Rag.Status status = chatResponse.getStatus();
                log.info("GRPC Received response: " + status + ":" + (status != null ? status.getCodeValue() : ""));
                Boolean isComplete = false;
                String answer = String.valueOf(stringBuffer.toString());

                if (status != null && status.getCode() == Rag.Status.Code.STREAM_END) {
                    Long seq = chatResponse.getSequence();
                    log.debug("GRPC Stream completed!!!(onNext):{},{}", chatroomDetail.getSeq(), seq);
                    if(seq == null || seq < 1L) {
                        seq = chatroomDetail.getSeq();
                    }
                    isComplete = true;
                    log.debug("GRPC Stream Sink Complete!!!(onNext) & ScheduleTask Stop");
                    sink.complete();
                    agentChatScheduleTask.stop();
//                    agentChatService.setChatroomTitle(chatroomDetail, answer);
                    agentChatService.setChatroomTitleFirstQ(chatroomDetail, question);

                    agentChatService.setChatRoomDetail(chatbot, chatroomDetail, answer, seq);
                    // 채팅 이후 chatroom update_at
                    agentChatService.updateChatroomTimestamp(chatroomDetail.getRoomId());

                    stringBuffer.setLength(0);  //한턴이 끝나면 stringBuffer를 비워줌.
                    agentChatService.sendSocketMessage(roomId, chatbot.getName(), "chat Complete(onNext)");
                } else if (status != null && status.getCode().getNumber() >= 500) { //오류코드 500 이상일 경우
                    isComplete = true;
                    log.debug("GRPC Stream error!!!(onNext)");
//                    answer = String.valueOf(errMessage);
                    sink.next("오류가 발생 하였습니다. 잠시후 다시 시도해 주세요. (ChatStreamObserverHanlder-GRPC ERROR:" + status.getCode().getNumber() + ")");
                    sink.complete();
//                    chatScheduleTask.stop();
//                    chatService.setChatroomTitle(chatroomDetail, answer);
//                    chatService.setChatRoomDetail(chatbot, chatroomDetail, answer);
//                    stringBuffer.setLength(0);  //한턴이 끝나면 stringBuffer를 비워줌.
//                    chatService.sendSocketMessage(roomId, chatbot.getName(), "chat Complete(onNext)");
                } else {
                    sink.next(chatResponse.getMsg());
                }

//                if(isComplete) {
//                    log.debug("GRPC Stream Sink Complete!!!(onNext) & ScheduleTask Stop");
//                    sink.complete();
//                    chatScheduleTask.stop();
//                    chatService.setChatroomTitle(chatroomDetail, answer);
//                    chatService.setChatRoomDetail(chatbot, chatroomDetail, answer);
//                    stringBuffer.setLength(0);  //한턴이 끝나면 stringBuffer를 비워줌.
//                    chatService.sendSocketMessage(roomId, chatbot.getName(), "chat Complete(onNext)");
//                }
            }

            @Override
            public void onError(Throwable t) {
                log.info("SINK STREAM STATUS: " + sink.isCancelled());
                log.info("GRPC Received response(chatHandler.onError): " + chatbot.getId() + ":" + question + ":" + sink.isCancelled() + ":" + t.getMessage());
                sink.next("오류가 발생했습니다. 잠시후 시도해 주세요. (ChatStreamObserver-onError)");
                sink.complete();
                stringBuffer.setLength(0);
                agentChatScheduleTask.stop();
                agentChatService.sendSocketMessage(roomId, chatbot.getName(), "chat Error:" + t.getMessage());
                log.error("GRPC ERROR : " + t.getMessage());
                connectionHandler.connectionReset(chatroomDetail.getRoomId());



//                throw new RuntimeException("GRPC ERROR", t);
//
//
//                log.error("proto error (1) : " + t.getMessage());
//                log.info("scheduler shutdown on Error");
//                chatScheduleTask.stop();
//                stringBuffer.setLength(0);
//                sink.error(t);
//                log.error("proto error (2) : sink.complete()");
//                sink.complete();
//                log.error("proto error (3) : channel.shutdown()");
//                channel.shutdownNow();
//                chatService.sendSocketMessage(roomId, chatbot.getName(), "chat Error:" + t.getMessage());
            }

            @Override
            public void onCompleted() {
                // 스트림이 완료되었을 때의 처리
                sink.complete();
                channel.shutdownNow();
                log.info("GRPC Received response(chatHandler.onComplete)" + chatbot.getId() + ":" + question + ":" + sink.isCancelled());
                log.debug("proto Stream completed!!!");
                stringBuffer.setLength(0);
                log.info("scheduler shutdown on complete");
                agentChatScheduleTask.stop();
//                scheduler.shutdownNow();
                agentChatService.sendSocketMessage(roomId, chatbot.getName(), "chat Complete");
//                closeChannel(roomId);
            }
        });


    }

    @Override
    public void onNext(ChatRequest chatRequest) {
        log.debug("ChatStreamObserverHandler:Overide.onNext");
        baseStreamObserver.onNext(chatRequest);
//        ChatScheduleTask task = new ChatScheduleTask(maxExecutionTime, chatScheduleManager, sink, memberId, chatbot.getId(), chatroomDetail.getRoomId(), question);
//        chatScheduleManager.scheduledFuture(task, 0, 15, TimeUnit.SECONDS);

//        final int maxExecutionCount = 10;
//        AtomicInteger executionCount = new AtomicInteger(0);
//
//        Runnable task = () -> {
//            int count = executionCount.incrementAndGet();
//            log.info("waiting response!!!!!!:" + count + ":" + chatRequest.getMsg());
//            if (count >= maxExecutionCount) {
//                log.info("schedule shutdown!!!:" + count + ":" + chatRequest.getMsg());
////                scheduler.shutdownNow();
//                Thread.currentThread().interrupt();
//                return;
//            }
//            String checkString = "|\uD83E\uDD16Pong!|";
//            sink.next(checkString);
//        };
//
//        if(scheduler == null || scheduler.isShutdown() || scheduler.isTerminated())
//            scheduler = scheduler = Executors.newScheduledThreadPool(1);
//        scheduler.scheduleAtFixedRate(task, 0, 15, TimeUnit.SECONDS);

    }

    @Override
    public void onError(Throwable throwable) {
        log.debug("ChatStreamObserverHandler:Overide.onError");
        baseStreamObserver.onError(throwable);
    }

    @Override
    public void onCompleted() {
        log.debug("ChatStreamObserverHandler:Overide.onComplete");
        baseStreamObserver.onCompleted();
    }


}
