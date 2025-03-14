package ai.maum.chathub.conf.message;

import ai.maum.chathub.api.common.BaseException;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class SlackMessenger {
    private final String normalTitle;
    private final String noticeTitle;
    private final boolean status;
    private final String mainChannel;
    private final MethodsClient client;
    private final ExecutorService es;

    SlackMessenger(@Value("${service.name}") String serviceName,
                   @Value("${service.slack.status}") boolean status,
                   @Value("${service.slack.channel}") String mainChannel,
                   @Value("${service.slack.token}") String token,
                   @Value("${service.slack.worker}") int worker,
                   Environment env) {
        normalTitle = "["
                + serviceName
                + "-"
                + env.getActiveProfiles()[0]
                + "]"
                + "\n";
        noticeTitle = "["
                + serviceName
                + "-"
                + env.getActiveProfiles()[0]
                + "]"
                + " - "
                + "NOTICE"
                + "\n";
        this.status = status;
        this.mainChannel = mainChannel;
        client = Slack.getInstance().methods(token);
        es = Executors.newFixedThreadPool(worker);
    }

    @PreDestroy
    public void close() {
        es.shutdown();
    }

    /**
     * 메인 슬랙 채널에 해당 메시지를 전송한다.
     * @param msg 전송 메시지
     * @author baekgol
     */
    public void send(Object msg) {
        chat(mainChannel, normalTitle, msg, false);
    }

    /**
     * 해당 슬랙 채널에 해당 메시지를 전송한다.
     * @param channel 채널
     * @param msg 전송 메시지
     * @author baekgol
     */
    public void send(String channel, Object msg) {
        chat(channel, normalTitle, msg, false);
    }

    /**
     * 메인 슬랙 채널에 해당 메시지를 전송한다.
     * 메시지를 공지하여 강조하고 싶을 때 사용한다.
     * @param msg 전송 메시지
     * @author baekgol
     */
    public void notice(Object msg) {
        chat(mainChannel, noticeTitle, msg, true);
    }

    /**
     * 해당 슬랙 채널에 해당 메시지를 전송한다.
     * 메시지를 공지하여 강조하고 싶을 때 사용한다.
     * @param channel 채널
     * @param msg 전송 메시지
     * @author baekgol
     */
    public void notice(String channel, Object msg) {
        chat(channel, noticeTitle, msg, true);
    }

    private void chat(String channel, String title, Object msg, boolean isNotice) {
        if(status) {
            ChatPostMessageRequest req = ChatPostMessageRequest.builder()
                    .channel(channel)
                    .build();

            req.setText(title + (!isNotice ? msg : "\n"
                    + "========================================================"
                    + "\n"
                    + msg
                    + "\n"
                    + "========================================================"));

            es.execute(() -> {
                try {
                    client.chatPostMessage(req);
                } catch(IOException | SlackApiException e) {
                    throw BaseException.of(e);
                }
            });
        }
    }
}
