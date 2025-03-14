package ai.maum.chathub.mybatis.mapper;

import ai.maum.chathub.mybatis.vo.ElasticVO;
import ai.maum.chathub.api.file.entity.SourceFileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ResourceCheckMapper {
//    public List<MonitorLogVO> selectLogByDate(Map<String, Timestamp> dates);
    public List<SourceFileEntity> selectGabageFiles();
    public List<String> selectValidESFilterPreFix();
    public List<ElasticVO> selectElasticEngineList();
}