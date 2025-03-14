package ai.maum.chathub;

import ai.maum.chathub.api.chat.handler.ChatSocketIOHandler;
import ai.maum.chathub.scheduler.ResourceCheckScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chathubtest")
public class TestController {
    private final ResourceCheckScheduler resourceCheckScheduler;
    private final ChatSocketIOHandler chatSocketIOHandler;
    @GetMapping("test")
    public String test() {

//        resourceCheckScheduler.testResourceChecker();
//        resourceCheckScheduler.copyESData();
        return "test";
    }

    @GetMapping("/send-message")
    public String sendSocketMessage(
            @RequestParam(name="room_id") String sessionId,
            @RequestParam(name="title") String title,
            @RequestParam(name="message") String message
    ) {
        chatSocketIOHandler.sendMessageToSession(sessionId, title, message);
        return "send!!!";
    }

}
