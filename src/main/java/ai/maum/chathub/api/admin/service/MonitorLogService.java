package ai.maum.chathub.api.admin.service;

import ai.maum.chathub.api.admin.entity.ChatMonitorLogEntity;
import ai.maum.chathub.api.admin.repo.ChatMonitorLogRepository;
import ai.maum.chathub.api.chatbot.entity.ChatbotEntity;
import ai.maum.chathub.api.chatbot.repo.ChatbotRepository;
import ai.maum.chathub.api.chatroom.entity.ChatroomDetailEntity;
import ai.maum.chathub.api.chatroom.repo.ChatroomDetailRepository;
import ai.maum.chathub.mybatis.mapper.MonitorLogMapper;
import ai.maum.chathub.mybatis.vo.MonitorLogVO;
import ai.maum.chathub.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorLogService {
    private final ChatMonitorLogRepository chatMonitorLogRepository;
    private final ChatroomDetailRepository chatroomDetailRepository;
    private final ChatbotRepository chatbotRepository;
    private final MonitorLogMapper monitorLogMapper;

//    public void  ChatMonitorLog(Long roomId, Long seq, String log) {
//        ChatMonitorLogEntity logEntity = new ChatMonitorLogEntity(roomId, seq, log);
//        chatMonitorLogRepository.save(logEntity);
//    }

    public void  ChatMonitorLog(Long roomId, Long seq, String title, String log) {
        ChatMonitorLog(roomId, seq, title, log, null);
    }

    public void  ChatMonitorLog(Long roomId, Long seq, String title, String log, Integer tokens) {
        ChatMonitorLogEntity logEntity = new ChatMonitorLogEntity(roomId, seq, title, log, tokens);
        chatMonitorLogRepository.save(logEntity);
    }

    public List<ChatMonitorLogEntity> getChatResultLog(Long roomId, Long seq) {
        if(roomId == null || seq == null) { //둘중 하나라도 null이면 가장 최근것.

            ChatroomDetailEntity chatRoomDetail = chatroomDetailRepository.findTopByOrderByIdDesc();
            roomId = chatRoomDetail.getRoomId();
            seq = chatRoomDetail.getSeq();
        }

        List<ChatMonitorLogEntity> logList = chatMonitorLogRepository.findChatMonitorLogEntitiesByRoomIdAndSeqOrderById(roomId, seq);

        return logList;

////        String result = new String();
//        List<String> result = new ArrayList<String>();
//
//        for(ChatMonitorLogEntity item:logList) {
////            result += item.getLog() + "\n\n-------------------------------\n\n";
//            result.add(item.getLog());
//        }
//
//        return result;
    }

    public void savePrompts(String prompt1, String prompt2, String strChatbotId) {
        Long chatbotId = Long.valueOf(strChatbotId);
        ChatbotEntity chatbot = chatbotRepository.getReferenceById(chatbotId);
        chatbot.setPromptRequirement(prompt1);
        chatbot.setPromptTail(prompt2);
        chatbotRepository.save(chatbot);
    }

//    public List<MonitorLogVO> getRecentLogs() {
//        return monitorLogMapper.selectRecentLog();
//    }

    public List<MonitorLogVO> getRecentLogList(String logDate) {
        Map<String, Timestamp> dates = DateUtil.convertDateStringToTimestampMap(logDate);
        if(dates == null)
            return new ArrayList<>();
        else
            return monitorLogMapper.selectLogByDate(dates);
    }
}
