package ai.maum.chathub.scheduler;

import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.conf.message.SlackMessenger;
import ai.maum.chathub.meta.LogLevelMeta;
import ai.maum.chathub.meta.RegexMeta;
import ai.maum.chathub.util.LogUtil;
import ai.maum.chathub.util.ZipUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = {"service.log.path", "service.log.zip.path"})
public class LogScheduler {
    @Value("${service.batch.status}")
    private boolean status;

    @Value("${service.log.path}")
    private String logPath;

    @Value("${service.log.zip.path}")
    private String zipPath;

    private final SlackMessenger slackMessenger;

    /**
     * 매일 일별 로그 파일들을 압축한다.
     * @author baekgol
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void schedule() {
        if(status) {
            try {
                LocalDate yesterday = LocalDate.now().minusDays(1);

                ZipUtil.zip(logPath,
                        RegexMeta.LOG_FILE_PREFIX
                                + yesterday
                                + RegexMeta.LOG_FILE_SUFFIX,
                        zipPath + "/" + yesterday + ".zip",
                        true);

                String msg = "["
                        + yesterday
                        + "]"
                        + " 로그를 압축하였습니다.";

                LogUtil.notice(msg, LogLevelMeta.INFO);
                slackMessenger.send(msg);
            } catch(Exception e) {
                throw BaseException.of(e);
            }
        }
    }
}
