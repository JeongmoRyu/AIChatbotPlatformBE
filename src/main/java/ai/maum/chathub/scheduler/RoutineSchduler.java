package ai.maum.chathub.scheduler;

import ai.maum.chathub.api.kakao.service.KakaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoutineSchduler {
    private final KakaoService kakaoService;
//    @Scheduled(fixedRate = 100 * 1000 ) // n초마다 실행
//    @Scheduled(fixedRate = n * 1000 ) // n초마다 실행
//    @Scheduled(fixedRate = n * 60 * 1000 ) // n분마다 실행
    public void executeRoutine() {
        log.debug("executeRoutine");
//        String accessToken = kakaoService.getAccessToken();
//        log.debug("accessToken:" + accessToken);
        kakaoService.sendFriendTalk();
    }
}
