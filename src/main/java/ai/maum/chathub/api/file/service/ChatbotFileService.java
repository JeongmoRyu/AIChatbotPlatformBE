package ai.maum.chathub.api.file.service;

import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.meta.ResponseMeta;
import ai.maum.chathub.api.file.dto.req.ChatbotFileSaveReq;
import ai.maum.chathub.api.file.entity.ChatbotFileEntity;
import ai.maum.chathub.api.file.repo.ChatbotFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotFileService {

    private final ChatbotFileRepository chatbotFileRepository;

    @Transactional
    public boolean deleteAndSave(ChatbotFileSaveReq param) {
        int chatbotId = param.getChatbotId();

        try {
            // 기존 저장된 파일들 모두 삭제.
            chatbotFileRepository.deleteByChatbotId(chatbotId);
            // 저장
            chatbotFileRepository.saveAll(
                    param.getFileIds().stream()
                            .map(fileId -> ChatbotFileEntity.builder()
                                    .chatbotId(chatbotId)
                                    .fileId(fileId)
                                    .build()
                            )
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            throw BaseException.of(ResponseMeta.FAILURE, e);
        }

        return true;
    }
}
