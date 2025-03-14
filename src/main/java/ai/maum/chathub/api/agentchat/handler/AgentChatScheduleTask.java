package ai.maum.chathub.api.agentchat.handler;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.ScheduledFuture;

@Slf4j
@Getter
@Setter
public class AgentChatScheduleTask implements Runnable {

    private int count = 0;
    private final int maxExecutionCount;
    private final AgentChatScheduleManager chatScheduleManager;
    private final FluxSink<String> sink;
    private final Long chatbotId;
    private final String question;
    private final Long roomId;
    private final String memberId;
//    private final ScheduledFuture<?> scheduledFuture;
    private ScheduledFuture<?> scheduledFuture;
    public AgentChatScheduleTask(int maxExecutionCount, AgentChatScheduleManager chatScheduleManager, FluxSink<String> sink, String memberId, Long chatbotId, Long roomId, String question
                            ) {
        this.maxExecutionCount = maxExecutionCount;
        this.chatScheduleManager = chatScheduleManager;
        this.sink = sink;
        this.chatbotId = chatbotId;
        this.question = question;
        this.roomId = roomId;
        this.memberId = memberId;
    }

    @Override
    public void run() {
        log.info("Running ChatScheduleTask... Count:" + count + ":" + memberId + ":" + chatbotId + ":" + roomId + ":"
                + (question != null && question.length() > 30 ? question.substring(0, 30) + "..." : question));
        count++;

        String checkString = "|\uD83E\uDD16Pong!|";
        sink.next(checkString);

        if(count >= maxExecutionCount) {
            log.info("Max execution count reached. Shutting down schduler.");
//            chatScheduleManager.shutdownScheduler();
            sink.next("\n******** response time is tool long. timeout reached ****************");
            sink.complete();
            chatScheduleManager.cancelTask(scheduledFuture);
        }
    }



    public void stop() {
        log.info("ChatScheduleTask... stop!!!:" + count + ":" + memberId + ":" + chatbotId + ":" + roomId + ":" + question);
//        chatScheduleManager.shutdownScheduler();
        chatScheduleManager.cancelTask(scheduledFuture);
    }
}
