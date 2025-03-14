package ai.maum.chathub.api.chatroom.service;

import ai.maum.chathub.api.chatbotInfo.repo.ChatbotInfoRepository;
import ai.maum.chathub.mybatis.mapper.ChatroomMapper;
import ai.maum.chathub.mybatis.vo.ChatHistoryVO;
import ai.maum.chathub.mybatis.vo.ChatroomDetailVO;
import ai.maum.chathub.mybatis.vo.ChatroomVO;
import ai.maum.chathub.api.chatbot.repo.ChatbotRepository;
import ai.maum.chathub.api.chatroom.entity.ChatroomDetailEntity;
import ai.maum.chathub.api.chatroom.entity.ChatroomEntity;
import ai.maum.chathub.api.chatroom.repo.ChatroomDetailRepository;
import ai.maum.chathub.api.chatroom.repo.ChatroomRepository;
import ai.maum.chathub.api.common.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// new chathistory
import ai.maum.chathub.api.chatroom.entity.ChatroomDateEntity;
import ai.maum.chathub.api.chatroom.entity.ChatroomDateGroup;
import ai.maum.chathub.api.chatroom.entity.ChatroomNewEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// new chathistory
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class ChatroomService {

    private final ChatroomRepository chatroomRepository;
    private final ChatroomDetailRepository chatroomDetailRepository;
    private final ChatbotRepository chatbotRepository;
    private final ChatroomMapper chatroomMapper;
    private final ChatbotInfoRepository chatbotInfoRepository;

    public ChatroomDetailEntity getChatRoomDetailById(Long chatroomDetailId) {
        return chatroomDetailRepository.getReferenceById(chatroomDetailId);
    }

    public List<ChatroomEntity> getChatroomList(Long chatbotId) {
//        return chatroomRepository.findChatroomEntitiesByChatbotIdOrderByUpdatedAt(chatbotId);
        return chatroomRepository.findChatroomEntitiesByChatbotIdOrderBySeqAscUpdatedAtDesc(chatbotId);
    }

    public List<ChatroomEntity> getMyChatroomList(Long chatbotId, String regUserId) {
//        return chatroomRepository.findChatroomEntitiesByChatbotIdOrderByUpdatedAt(chatbotId);
//        return chatroomRepository.findChatroomEntitiesByChatbotIdAndRegUserIdOrderBySeqAscUpdatedAtDesc(chatbotId, regUserId);
        return chatroomRepository.findByChatbotIdAndRegUserIdAndTitleIsNotNullOrderBySeqAscUpdatedAtDesc(chatbotId, regUserId);

    }

    public List<ChatroomDetailEntity> getChatroomDetail(Long chatroomId) {
        return chatroomDetailRepository.findChatroomDetailEntitiesByRoomIdOrderBySeqAscCreatedAtAsc(chatroomId);
    }

    public List<ChatroomDetailEntity> getChatroomDetailByRegUserId(String regUserId) {
        List<ChatroomEntity> chatroomEntities = chatroomRepository.findChatroomEntitiesByRegUserId(regUserId);
        List<ChatroomDetailEntity> chatroomDetailEntityList = new ArrayList<ChatroomDetailEntity>();
        if(chatroomEntities != null && chatroomEntities.size() > 0L) {
            Long chatroomId = chatroomEntities.get(0).getId();
            chatroomDetailEntityList = chatroomDetailRepository.findChatroomDetailEntitiesByRoomIdOrderBySeqAscCreatedAtAsc(chatroomId);
        }
        return chatroomDetailEntityList;
    }

    public ChatroomEntity setChatroom(ChatroomEntity chatroom) {
        //chatbot이 있는지 확인
//        if(chatbotRepository.existsById(chatroom.getChatbotId()))
        if(chatbotInfoRepository.existsById(chatroom.getChatbotId()))
            return chatroomRepository.save(chatroom);
        else {
            throw BaseException.of("챗봇이 없습니다.");
        }
    }

    public ChatroomEntity getChatroom(Long chatroomId) {
        return chatroomRepository.findById(chatroomId).orElse(null);
//        return chatroomRepository.getReferenceById(chatroomId);
    }

    @Transactional
    public int setChatroomTitle(Long id, String title) {
        ChatroomVO chatroomVO = new ChatroomVO();
        chatroomVO.setId(id);
        chatroomVO.setTitle(title);
        return chatroomMapper.updateChatroomTitle(chatroomVO);
    }
//     첫 질문 chatroom title update
    @Transactional
    public int setChatroomTitleFirstQ(Long id, String title) {

        ChatroomVO currentChatroom = getChatRoomByIdFromMapper(id);
        if (currentChatroom != null && currentChatroom.getTitle() != null && !currentChatroom.getTitle().isEmpty()) {
            return 0;
        }
        ChatroomVO chatroomVO = new ChatroomVO();
        chatroomVO.setId(id);
        chatroomVO.setTitle(title);
        return chatroomMapper.updateChatroomTitle(chatroomVO);
    }
    //  질문 후 updated_at 업데이트
    @Transactional
    public int updateChatroomTimestamp(Long roomId) {
        if (roomId == null || roomId <= 0) {
            return 0;
        }

        ChatroomVO chatroomVO = new ChatroomVO();
        chatroomVO.setId(roomId);
        chatroomVO.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return chatroomMapper.updateChatroomTimestamp(chatroomVO);
    }



    public ChatroomDetailEntity setChatroomDetail(ChatroomDetailEntity chatroomDetail) {
        return chatroomDetailRepository.save(chatroomDetail);
    }

    public Long getChatroomSeq(Long chatroomId) {
        Long seq = chatroomDetailRepository.findMaxMaxSeqByRoomId(chatroomId);
        if(seq == null)
            seq = 0L;
        return seq + 1;
    }

    public List<ChatroomDetailEntity> getChatroomDetailByRoomIdAndSeq(Long chatroomId, Long seq) {
        return chatroomDetailRepository.findChatroomDetailEntitiesByRoomIdAndSeq(chatroomId, seq);
    }

    public int setFeedBack(Long chatroomId, Long seq, String feedback, String userName) {
        return chatroomDetailRepository.updateFeedbackByRoomIdAndSeqAndRole(chatroomId, seq, "assistant", feedback, userName);
    }

    public ChatroomVO getChatRoomByIdFromMapper(Long id) {
        return chatroomMapper.selectChatroomById(id);
    }

    public ChatroomDetailVO getChatRoomDetailByIdFromMapper(Long id) {
        return chatroomMapper.selectChatroomDetailById(id);
    }

    public ChatroomDetailEntity findTopByOrderByIdDesc() {
        return chatroomDetailRepository.findTopByOrderByIdDesc();
    }

    public List<ChatHistoryVO> getChatHistoryForMultiturn(Long roomId, Integer multiTurn) {
        Map<String, Object> param = new HashMap<String,Object>();
        param.put("room_id", roomId);
        if(multiTurn != null && multiTurn > 0)
            param.put("multi_turn", multiTurn);
        return chatroomMapper.selectChatHisotryForMultiturn(param);
    }

    public ChatroomVO getChatroomByRegUserID(String regUserId) {
        return chatroomMapper.selectChatroomByRegUserIdLimitOne(regUserId);
    }

    public ChatroomVO getChatroomByRegUserIDAndChatbotId(String regUserId, Long chatbotId) {
        Map<String,Object> paramMap = new HashMap<String,Object>();
        paramMap.put("reguserId", regUserId);
        paramMap.put("chatbotId", chatbotId);
        return chatroomMapper.selectChatroomByRegUserIdAndChatbotIdLimitOne(paramMap);
    }

    //  일자로 이전 대화 목로 list 가져오기
    public ChatroomDateEntity getChatroomsByDateGroup(Long chatbotId, String regUserId) {
        // Get all chatrooms ordered by updatedAt
        List<ChatroomEntity> allChatrooms = chatroomRepository
                .findByChatbotIdAndRegUserIdAndTitleIsNotNullOrderByUpdatedAtDesc(chatbotId, regUserId);

        ChatroomDateEntity response = new ChatroomDateEntity();
        List<ChatroomDateGroup> groups = new ArrayList<>();

        LocalDate today = LocalDate.now();
        Timestamp todayStart = Timestamp.valueOf(today.atStartOfDay());
        Timestamp tomorrowStart = Timestamp.valueOf(today.plusDays(1).atStartOfDay());

        // Create a map to group chatrooms by date
        Map<String, List<ChatroomNewEntity>> chatroomsByDate = new LinkedHashMap<>();

        // Add "오늘" (Today) as the first group
        chatroomsByDate.put("오늘", new ArrayList<>());

        // Process each chatroom and place it in the appropriate date group
        for (ChatroomEntity chatroom : allChatrooms) {
            ChatroomNewEntity dto = new ChatroomNewEntity(chatroom);

            if (chatroom.getUpdatedAt().after(todayStart) && chatroom.getUpdatedAt().before(tomorrowStart)) {
                chatroomsByDate.get("오늘").add(dto);
            } else {
//                LocalDateTime updatedDate = chatroom.getUpdatedAt().toLocalDateTime();
//                String dateKey = updatedDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
                LocalDate updatedDate = chatroom.getUpdatedAt().toLocalDateTime().toLocalDate();
                String dateKey = updatedDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));

                if (!chatroomsByDate.containsKey(dateKey)) {
                    chatroomsByDate.put(dateKey, new ArrayList<>());
                }
                chatroomsByDate.get(dateKey).add(dto);
            }
        }
        for (List<ChatroomNewEntity> chatrooms : chatroomsByDate.values()) {
            chatrooms.sort((c1, c2) -> c2.getUpdatedAt().compareTo(c1.getUpdatedAt()));
        }
        if (chatroomsByDate.get("오늘").isEmpty()) {
            chatroomsByDate.remove("오늘");
        }

        // Convert map to list of groups
        for (Map.Entry<String, List<ChatroomNewEntity>> entry : chatroomsByDate.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                ChatroomDateGroup group = new ChatroomDateGroup();
                group.setDateLabel(entry.getKey());
                group.setChatrooms(entry.getValue());
                groups.add(group);
            }
        }

        response.setGroups(groups);
        return response;
    }
}
