package ai.maum.chathub.conf.webclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.corundumstudio.socketio.SocketIOServer;

import javax.annotation.PreDestroy;
import java.net.BindException;

@Slf4j
@Configuration
public class SocketIOConfig {

    @Value("${service.socketio.port}")
    private Integer port;

    @Value("${service.socketio.hostname}")
    private String hostname;

    @Value("${service.socketio.retry.count}")
    private Integer retryCount;

    @Value("${service.socketio.retry.interval}")
    private Long retryInterval; // 재시도 간격 (밀리초)

    private int pingInterval = 20 * 1000;
    private int pingTimeout = 60 * 1000;

    private SocketIOServer server; // 서버 인스턴스 저장

    @Bean
    public SocketIOServer socketIOServer() {
//        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
//        config.setHostname("localhost");
//        config.setPort(9994); // NGINX를 사용하여 외부에서 접근할 경우, 실제 포트는 NGINX가 처리합니다.
//
//        // 추가 설정 예시
//        config.setOrigin(":*:");
//        return new SocketIOServer(config);
//    }

        for (int attempt = 1; attempt <= retryCount; attempt++) {
            try {
                com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration(); // Socket.IO의 Configuration을 전체 경로로 사용
                config.setHostname(hostname);
                config.setPort(port);
                config.setOrigin(":*:");

                // 여기에 추가 설정
//                config.setPingInterval(pingInterval);
//                config.setPingTimeout(pingTimeout);
//                config.setUpgradeTimeout(3 * 1000);

//                SocketIOServer server = new SocketIOServer(config);
                server = new SocketIOServer(config);
                server.start(); // 여기에서 BindException이 발생할 수 있음
                log.info("Socket.io Bind 성공: " + attempt);
                // 종료 훅 등록
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    server.stop();
                }));
                return server; // 성공시 서버 반환
            } catch (Exception e) {
                if (e instanceof BindException) {
                    log.info("Socket.io BindException 발생, 재시도 중... 시도 횟수: " + attempt + ":" + e.getMessage());
                    if (attempt < retryCount) {
                        try {
                            Thread.sleep(retryInterval);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        throw new RuntimeException("SocketIOServer 포트 바인딩에 실패했습니다.", e);
                    }
                } else {
                    throw e; // BindException 이외의 예외는 바로 던짐
                }
            }
        }
        throw new IllegalStateException("SocketIOServer를 시작할 수 없습니다.");
    }

    @PreDestroy
    public void onDestroy() throws Exception {
        if (server != null) {
            server.stop();
            log.info("Socket.io 서버가 정상적으로 종료되었습니다.");
        }
    }
}