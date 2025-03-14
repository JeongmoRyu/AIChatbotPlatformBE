package ai.maum.chathub.api.admin.service;

import ai.maum.chathub.api.admin.entity.TempUserMappingEntity;
import ai.maum.chathub.api.admin.repo.TempMessageRepository;
import ai.maum.chathub.api.admin.repo.TempUserMappingRepository;
import ai.maum.chathub.api.admin.entity.TempMessageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TempUserMappingService {
    private final TempUserMappingRepository tempUserMappingRepository;
    private final TempMessageRepository tempMessageRepository;

    public TempUserMappingEntity getTempUserMapping(String userName, String identyNo) {
        return tempUserMappingRepository.findTempUserMappingEntityByUserNameAndIdentityNo(userName, identyNo);
    }

    public TempUserMappingEntity getTempUserMappingWithShortNo(String userName, String identyNo) {

        TempUserMappingEntity rtnEntity = null;

        List<TempUserMappingEntity> items = tempUserMappingRepository.findTempUserMappingEntitiesByUserName(userName);
        if(items != null)
            for(TempUserMappingEntity item:items) {
                if(item.getIdentityNo().length() > 4)
                    if(item.getIdentityNo().substring(item.getIdentityNo().length()-4).equals(identyNo)) {
                        rtnEntity = item;
                        break;
                    }
            }
        return rtnEntity;
    }

    public TempUserMappingEntity getTempUserMappingWithUserName(String userName) {

        TempUserMappingEntity rtnEntity = null;

        List<TempUserMappingEntity> items = tempUserMappingRepository.findTempUserMappingEntitiesByUserName(userName);

        if(items != null && items.size() > 0) {
            rtnEntity = items.get(0);
        }

        return rtnEntity;
    }

    public TempMessageEntity setTempMessageEntity(String botUserKey, String message) {
        TempMessageEntity item = new TempMessageEntity();
        item.setBotUserKey(botUserKey);
        item.setMessage(message);
        return tempMessageRepository.save(item);
    }
}
