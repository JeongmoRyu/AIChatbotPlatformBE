package ai.maum.chathub.api.admin;

import ai.maum.chathub.api.chatbot.service.ChatbotService;
import ai.maum.chathub.api.chatroom.service.ChatroomService;
import ai.maum.chathub.api.admin.service.MonitorLogService;
import ai.maum.chathub.mybatis.vo.MonitorLogVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;
import java.util.Locale;

@Slf4j
@Controller
@RequestMapping("/maum-admin")
@RequiredArgsConstructor
@Tag(name="Admin-운영", description="서비스 오픈전 테스트 및 운영을 위한 임시 Controller")
public class AdminManagementController {

    private final MonitorLogService monitorLogService;
    private final ChatroomService chatroomService;
    private final ChatbotService chatbotService;

    @GetMapping("test")
    public String test(Model model) {
        model.addAttribute("name", "ftl user");
        return "test";
    }

    @GetMapping("llmtest")
    public String llmtest(
    ) {
        return "llmtest";
    }

    @GetMapping("monitor")
    public String logMonitor(Model model) {

//        String log = monitorLogService.getChatResultLog(null, null);

        //가장 최근 대화의 내용을 가져옴.
//        ChatroomDetailEntity chatroomDetail = chatroomService.findTopByOrderByIdDesc();
//        Long roomId = chatroomDetail.getRoomId();
//        Integer seq = chatroomDetail.getSeq();
//        ChatroomEntity chatroomEntity = chatroomService.getChatroom(roomId);
//        ChatbotEntity chatbot = chatbotService.getChatbotById(chatroomEntity.getChatbotId());
//
//        List<ChatMonitorLogEntity> logs = monitorLogService.getChatResultLog(roomId, seq);

//        model.addAttribute("logData", "이것은 로그!!!");
//        model.addAttribute("prompt_1", "요것은 프롬프트1!!!");
//        model.addAttribute("prompt_2", "이녀석은 프롬프트2!!!");

        DateFormatter dateFormatter = new DateFormatter("yyyy/MM/dd");
        String nowDate = dateFormatter.print(new Date(), Locale.getDefault());

//        List<MonitorLogVO> recentLog = monitorLogService.getRecentLogs();
        List<MonitorLogVO> recentLog = monitorLogService.getRecentLogList(nowDate);
        model.addAttribute("nowDate", nowDate);
        model.addAttribute("recentLog", recentLog);

//        model.addAttribute("logData", logs);
//        model.addAttribute("prompt_1", chatbot.getPromptRequirement());
//        model.addAttribute("prompt_2", chatbot.getPrompt_tail());
//        model.addAttribute("chatbot_id", chatbot.getId());


        //최초에는 가장 최근에 만들어진 대화의 로그를 가져와서 보여준다.


        return "LogMonitor";
    }
}
