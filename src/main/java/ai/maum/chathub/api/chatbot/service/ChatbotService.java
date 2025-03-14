package ai.maum.chathub.api.chatbot.service;

import ai.maum.chathub.api.chatbot.entity.ChatbotContentEntity;
import ai.maum.chathub.api.chatbot.repo.ChatbotContentRepository;
import ai.maum.chathub.api.chatbot.repo.ChatbotRepository;
import ai.maum.chathub.mybatis.mapper.ChatbotMapper;
import ai.maum.chathub.mybatis.vo.ChatbotVO;
import ai.maum.chathub.util.LogUtil;
import ai.maum.chathub.api.chat.handler.ChatGrpcConnectionHandler;
import ai.maum.chathub.api.chatbot.dto.ChatbotContent;
import ai.maum.chathub.api.chatbot.dto.ChatbotContentCard;
import ai.maum.chathub.api.chatbot.dto.ChatbotContentComment;
import ai.maum.chathub.api.chatbot.dto.ChatbotContentTitle;
import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.api.common.BaseResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {
    private final ChatbotRepository chatbotRepository;
    private final ChatbotContentRepository chatbotContentRepository;
    private final ChatbotMapper chatbotMapper;
    private final ChatGrpcConnectionHandler chatGrpcConnectionHandler;

//    public ChatbotEntity updateChatbot(ChatbotEntity chatbot) {
//        chatbot = chatbotRepository.save(chatbot);
//        //챗봇이 수정되면 기존 grpc connection을 리셋해준다.
//        chatGrpcConnectionHandler.resetChannelByChatbotId(chatbot.getId());
//        return chatbot;
//    }

    @Transactional
    public Object processChatbotRemoval(String userId, Long chatbotId) {
        try {
            boolean isExist = chatbotRepository.existsByUserIdAndId(userId, chatbotId);
            if (!isExist) {
                return BaseResponse.failure("존재하지 않는 chatbot 입니다.");
            }

            int iDelete = chatbotRepository.deleteChatbotEntityByUserIdAndId(userId, chatbotId);

            log.debug("iDelete:{}", iDelete);

            if (iDelete > 0)
                return BaseResponse.success();
            else
                return BaseResponse.failure();
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            return BaseResponse.failure(e);
        }
    }

//    public List<ChatbotEntity> getChatbotList() {
//        return chatbotRepository.findAll();
//    }
//    public List<ChatbotEntity> getChatbotList(String userId) {
//        return chatbotRepository.findChatbotEntitiesByUserId(userId);
//    }
//
//    public ChatbotEntity getChatbotById(Long chatbotId) {
//        return chatbotRepository.findChatbotEntityById(chatbotId);
//    }


    public ChatbotVO getChatbotByIdWithMapper(Long id) {
        return chatbotMapper.selectChatbotById(id);
    }


//    public List<ChatbotEntity> getChatbot(Long chatbotId) {
//
//        List<ChatbotEntity> chatbotList = new ArrayList<ChatbotEntity>();
//
//        if(chatbotId == 0) {
//            return chatbotRepository.findAll();
//        } else {
//            ChatbotEntity chatbot = chatbotRepository.findById(chatbotId).orElse(null);
//            if(chatbot != null) {
//                chatbotList.add(chatbot);
//            }
//        }
//
//        return chatbotList;
//    }

//    public Object getChatbotList (String userId, Long chatbotId) {
//        List<ChatbotEntity> chatbotList = new ArrayList<ChatbotEntity>();
//
//        if (chatbotId == null || chatbotId == 0L)
//            chatbotList = chatbotRepository.findChatbotEntitiesByUserId(userId);
//        else
//            chatbotList = chatbotRepository.findChatbotEntitiesByUserIdAndId(userId, chatbotId);
//
//        return chatbotList;
//    }

    public ChatbotContent getChatBotContent(Long chatbotId) {
        ChatbotContent chatbotContent = new ChatbotContent();
        List<ChatbotContentCard> chatbotContentCardList = new ArrayList<ChatbotContentCard>();
        List<ChatbotContentEntity> chatbotContentList = chatbotContentRepository.findByChatbotIdOrderByTypeCdAscSeqAsc(chatbotId);

        if(chatbotContentList == null || chatbotContentList.size() < 1) {
            //해당 챗봇에 contents가 없으면 기본 컨텐츠 (chatbotId = 0) 인 contents를 리턴
            chatbotContentList = chatbotContentRepository.findByChatbotIdOrderByTypeCdAscSeqAsc(0L);
        }

        LogUtil.debug("chatbotContentList:" + chatbotContentList.size());

        for(ChatbotContentEntity item:chatbotContentList) {
            switch(item.getTypeCd()) {
                case("TITLE"):
                    chatbotContent.setTitle(
                            new ChatbotContentTitle(item.getText())
                    );
                    break;
                case("COMMENT"):
                    chatbotContent.setComment(
                            new ChatbotContentComment(
                                    item.getImg(),
                                    item.getText()
                            )
                    );
                    break;
                case("CARD"):
                    item.getImg();
                    item.getTitle();
                    item.getText();

                    chatbotContentCardList.add(
                      new ChatbotContentCard(
                              item.getImg(),
                              item.getTitle(),
                              item.getText()
                      )
                    );
                    break;
                default:
                    break;
            }

            chatbotContent.setCards(chatbotContentCardList);

        }

        if(chatbotContent.getTitle() == null
                && chatbotContent.getComment() == null
                && chatbotContent.getCards() == null) {
            throw BaseException.of("데이터가 없습니다.");
//            chatbotContent = null;
        }

        return chatbotContent;
    }
}
