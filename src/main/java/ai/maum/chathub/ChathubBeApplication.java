package ai.maum.chathub;

import ai.maum.chathub.api.chat.handler.ChatScheduleManager;
import ai.maum.chathub.api.agentchat.handler.AgentChatScheduleManager;
import ai.maum.chathub.conf.message.SlackMessenger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableAspectJAutoProxy
@EnableJpaAuditing
@RequiredArgsConstructor
public class ChathubBeApplication implements CommandLineRunner, DisposableBean {

//    @Value("${service.grpc.chat.max-execution-count}")
//    private int maxExecutionCount;
    @Value("${service.grpc.chat.max-execution-time}")
    private int maxExecutionTime;

    @Value("${service.grpc.chat.schedule-pool-size}")
    private int schedulePoolSize;

    private final SlackMessenger slackMessenger;
//    private final int maxExecutionTime = 120;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ChathubBeApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run(args);
    }

    @Override
    public void run(String... args) {
        slackMessenger.notice("서비스 시작");
    }

    @Override
    public void destroy() {
        slackMessenger.notice("서비스 종료");
    }

    @Bean
    public ChatScheduleManager scheduleManager() {
        return new ChatScheduleManager(schedulePoolSize, maxExecutionTime * 1000);
    }

    @Bean
    public AgentChatScheduleManager chatScheduleManager() {
        return new AgentChatScheduleManager(schedulePoolSize, maxExecutionTime * 1000);
    }
}
