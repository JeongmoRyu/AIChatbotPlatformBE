package ai.maum.chathub.conf.security.cors.service;

import ai.maum.chathub.conf.security.cors.entity.CorsDomainEntity;
import ai.maum.chathub.conf.security.cors.repo.CorsDomainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CorsDomainService {

    @Autowired
    private CorsDomainRepository corsDomainRepository;

    public List<String> getAllowedOrigins() {
        // DB에서 도메인 리스트 가져오기
        return corsDomainRepository.findAllDomains();
    }
}