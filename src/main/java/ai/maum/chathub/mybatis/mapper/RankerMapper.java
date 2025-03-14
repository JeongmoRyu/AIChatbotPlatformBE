package ai.maum.chathub.mybatis.mapper;

import ai.maum.chathub.api.ranker.dto.res.RankerHistoryDetailRes;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface RankerMapper {
    public List<String> selectModelDetailList(Long rankerId);
    public RankerHistoryDetailRes selectModelDetail(Long rankerId);
}
