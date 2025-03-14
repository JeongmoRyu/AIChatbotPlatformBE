package ai.maum.chathub.api.engine.service;

import ai.maum.chathub.mybatis.mapper.EngineMapper;
import ai.maum.chathub.mybatis.vo.ElasticVO;
import ai.maum.chathub.mybatis.vo.EngineVO;
import ai.maum.chathub.mybatis.vo.LibraryVO;
import ai.maum.chathub.api.engine.entity.EngineEntity;
import ai.maum.chathub.api.engine.repo.EngineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
@RequiredArgsConstructor
@Service
public class EngineService {
    private final EngineRepository engineRespository;
    private final EngineMapper engineMapper;

    public List<EngineEntity> getEngineList(String type, String vendor, Boolean withApiKey, Long organizationId) {

        List<EngineEntity> engineList = new ArrayList<EngineEntity>();

        if(type != null && !type.isEmpty() && vendor != null && !vendor.isEmpty()) {
            engineList = engineRespository.findByTypeAndVendorOrderBySeqAsc(type, vendor, organizationId);
        } else if ( type != null && !type.isEmpty() && (vendor == null || vendor.isEmpty()) ) {
            engineList = engineRespository.findByTypeOrderByTypeAscSeqAsc(type, organizationId);
        } else
            engineList = engineRespository.findAllOrderByTypeAscVendorAscSeqAsc(organizationId);

        if(withApiKey != null && withApiKey == false)
            engineList.forEach(EngineEntity::removeSecretValue);

        return engineList;
    }

    public EngineEntity getEngine(Long id) {
        return engineRespository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Engine not found with id: " + id));
    }

    public EngineVO getEngineByIdWithMapper(Long id) {
        return engineMapper.selectEngineById(id);
    }

    public ElasticVO getElasticEngineByIdWithMapper(Long id) {
        return engineMapper.selectElasticEngineInfoById(id);
    }
    public List<ElasticVO> getElasticEngineList() {
        return engineMapper.selectElasticEngineList();
    }
    public List<LibraryVO> getLibraryList() {
        return engineMapper.selectLibraryList();
    }
}
