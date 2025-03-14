package ai.maum.chathub.mybatis.mapper;

import ai.maum.chathub.mybatis.vo.ChatHistoryVO;
import ai.maum.chathub.mybatis.vo.ChatroomDetailVO;
import ai.maum.chathub.mybatis.vo.ChatroomVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface ChatroomMapper {
    public ChatroomVO selectChatroomById(Long id);
    public ChatroomDetailVO selectChatroomDetailById(Long id);
    int updateChatroomTitle(ChatroomVO chatroomVO);
    public List<ChatHistoryVO> selectChatHisotryForMultiturn(Map<String,Object> param);
    public ChatroomVO selectChatroomByRegUserIdLimitOne(String regUserId);
    public ChatroomVO selectChatroomByRegUserIdAndChatbotIdLimitOne(Map<String,Object> param);

    int updateChatroomTimestamp(ChatroomVO chatroomVO);
}