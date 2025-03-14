package ai.maum.chathub.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Mapper
@Repository
public interface StatisticsMapper {
    String statDay(Map<String, Object> param);
    String statWeek(Map<String, Object> param);
    String statMonth(Map<String, Object> param);
    String statPeriod(Map<String, Object> param);
    String apiStatDay(Map<String, Object> param);
    String apiStatWeek(Map<String, Object> param);
    String apiStatMonth(Map<String, Object> param);
    String apiStatPeriod(Map<String, Object> param);

    String statDayRanker(Map<String, Object> param);
    String statWeekRanker(Map<String, Object> param);
    String statMonthRanker(Map<String, Object> param);
    String statPeriodRanker(Map<String, Object> param);
    String apiStatDayRanker(Map<String, Object> param);
    String apiStatWeekRanker(Map<String, Object> param);
    String apiStatMonthRanker(Map<String, Object> param);
    String apiStatPeriodRanker(Map<String, Object> param);
}