package ai.maum.chathub.mybatis.mapper;

import ai.maum.chathub.mybatis.vo.ElasticVO;
import ai.maum.chathub.mybatis.vo.EngineVO;
import ai.maum.chathub.mybatis.vo.LibraryVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface EngineMapper {
//    public List<ChatbotVO> selectChatbotList();
    public EngineVO selectEngineById(Long id);
    public ElasticVO selectElasticEngineInfoById(Long id);
    public List<ElasticVO> selectElasticEngineList();
    public List<LibraryVO> selectLibraryList();
    public void setElasticApiKey(ElasticVO elasticInfo);
}