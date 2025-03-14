package ai.maum.chathub.mybatis.mapper;

import ai.maum.chathub.mybatis.vo.MonitorLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface MonitorLogMapper {
//    public List<MonitorLogVO> selectRecentLog();
    public List<MonitorLogVO> selectLogByDate(Map<String, Timestamp> dates);
    public List<MonitorLogVO> selectLogForFunctionCallBySeq(Long seq);
}