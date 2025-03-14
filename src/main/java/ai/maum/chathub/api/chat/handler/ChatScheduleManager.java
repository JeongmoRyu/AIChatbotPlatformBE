package ai.maum.chathub.api.chat.handler;

import ai.maum.chathub.util.DateUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class ChatScheduleManager {
    private final ScheduledExecutorService schedulerPool;
//    private final Map<ScheduledFuture<?>, Long> taskStartTimes;
    private final Map<ScheduledFuture<?>, ScheduleObject> scheduleObjectMap;
    private final long maxExecutionTime;

    @Getter
    @Setter
    public static class ScheduleObject {
        private Long taskStartTimes;
        private String userKey;
        private Long botId;
        private Long roomId;

        public ScheduleObject() {
        }

        public ScheduleObject(Long taskStartTimes, String userKey, Long botId, Long roomId) {
            this.taskStartTimes = taskStartTimes;
            this.userKey = userKey;
            this.botId = botId;
            this.roomId = roomId;
        }
    }

    public ChatScheduleManager(int poolSize, long maxExecutionTime) {
        this.schedulerPool = Executors.newScheduledThreadPool(poolSize);
//        this.taskStartTimes = new ConcurrentHashMap<>();
        this.scheduleObjectMap = new ConcurrentHashMap<>();
        this.maxExecutionTime = maxExecutionTime;
        startMonitoring();
    }

    public ScheduledFuture<?> scheduledFuture(Runnable task, long initialDelay, long period, TimeUnit unit) {
        ScheduledFuture<?> scheduledFuture = schedulerPool.scheduleAtFixedRate(task, initialDelay, period, unit);
        ScheduleObject scheduleObject = new ScheduleObject();
        try {
            ChatScheduleTask chatScheduleTask = (ChatScheduleTask) task;
            scheduleObject = new ScheduleObject(System.currentTimeMillis(), chatScheduleTask.getMemberId(), chatScheduleTask.getChatbotId(), chatScheduleTask.getRoomId());
            log.info("Cast task to ChatScheduleTask success!!! - second scheduledFuture call");
        } catch (Exception e) {
            log.info("Cast task to ChatScheduleTask fail!!! - first scheduledFuture call");
        }
//        taskStartTimes.put(scheduledFuture, System.currentTimeMillis());
        scheduleObjectMap.put(scheduledFuture, scheduleObject);
        return scheduledFuture;
    }

    public void shutdownScheduler() {
        schedulerPool.shutdown();
        try {
            if(!schedulerPool.awaitTermination(1, TimeUnit.SECONDS)) {

            }
        } catch (InterruptedException e) {
            log.info("shutdownScheduler interrupted:" + e.getMessage());
            schedulerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void startMonitoring() {
        log.info("Chat Schduler monitoring start....");
        schedulerPool.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
//            for(Map.Entry<ScheduledFuture<?>, Long> entry : taskStartTimes.entrySet()) {
            log.info("Chat Schduler monitoring execute : " + DateUtil.convertToStringByMs(currentTime));
            for(Map.Entry<ScheduledFuture<?>, ScheduleObject> entry : scheduleObjectMap.entrySet()) {
                if(entry != null && entry.getValue() != null)
                    log.info("Schduler check:" + entry.getValue().getUserKey() + ":" + entry.getValue().getBotId() + ":" + entry.getValue().getRoomId() + DateUtil.convertToStringByMs(entry.getValue().getTaskStartTimes()));
                else
                    log.info("Schduler check entry is null:" + entry.getKey().toString() + ":" + entry.getKey().isCancelled() + ":" + entry.getKey().isDone());
                Long createTime = entry.getValue().getTaskStartTimes();
                if(currentTime - createTime > maxExecutionTime) {
                    entry.getKey().cancel(true);
                    log.info("Schduler forceully terminated due to exceeding max execution time.:" + entry.getValue().getUserKey() + ":" + entry.getValue().getBotId() + ":" + entry.getValue().getRoomId());
                    scheduleObjectMap.remove(entry.getKey());
                }
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

//    public void cancelTask()
    public void cancelTask(ScheduledFuture<?> scheduledFuture) {
        scheduledFuture.cancel(true);
        ScheduleObject scheduleObject = scheduleObjectMap.get(scheduledFuture);
        if(scheduleObject != null) {
            log.info("cancelTask:" + scheduleObject.getUserKey() + ":" + scheduleObject.getBotId()
                    + ":" + scheduleObject.getRoomId() + ":" + DateUtil.convertToStringByMs(scheduleObject.getTaskStartTimes()));
            scheduleObjectMap.remove(scheduledFuture);
        } else {
            log.info("cancelTask null!!!");
        }
    }

}
