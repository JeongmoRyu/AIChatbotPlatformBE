package ai.maum.chathub.mybatis.mapper;

import ai.maum.chathub.mybatis.vo.ChatbotVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ChatbotMapper {
    public List<ChatbotVO> selectChatbotList();

    //public long insertChatbot(ChatbotVO);
    public ChatbotVO selectChatbotById(Long id);
}