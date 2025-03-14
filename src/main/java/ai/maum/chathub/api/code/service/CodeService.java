package ai.maum.chathub.api.code.service;

import ai.maum.chathub.api.code.dto.res.CodeRes;
import ai.maum.chathub.api.code.repo.CodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeService {
    private final CodeRepository codeRepository;

    public List<CodeRes> getCoeList(String cdgroupId) {
        return codeRepository.findByCdGroupId(cdgroupId);
    }
}
