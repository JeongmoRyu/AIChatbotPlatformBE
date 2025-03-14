package ai.maum.chathub.api.statistics.service;

import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.statistics.dto.res.ChathubStatsRes;
import ai.maum.chathub.api.statistics.model.*;
import ai.maum.chathub.meta.ResponseMeta;
import ai.maum.chathub.mybatis.mapper.StatisticsMapper;
import ai.maum.chathub.util.DateUtil;
import ai.maum.chathub.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final StatisticsMapper statisticsMapper;

    public BaseResponse<ChathubStatsRes> getStats(String serviceType, String fromDate, String toDate, String statType) {

        ChathubStatsRes stats = new ChathubStatsRes();

        StatBoard llmTokenStats = new StatBoard();
        StatBoard embedTokenStats = new StatBoard();
        StatBoard llmApiStats = new StatBoard();
        StatBoard embedApiStats = new StatBoard();

        switch(statType.toUpperCase()) {
            case("DAY"):
                log.debug("오늘");
                llmTokenStats = getStatToday(serviceType, fromDate, "LLM", null);
                embedTokenStats = getStatToday(serviceType, fromDate, "RAG", null);
                llmApiStats = getStatToday(serviceType, fromDate, "LLM", "API");
                embedApiStats = getStatToday(serviceType, fromDate, "RAG", "API");
                break;
            case("WEEK"):
                log.debug("금주");
                llmTokenStats = getStatThisWeek(serviceType, fromDate, "LLM", null);
                embedTokenStats = getStatThisWeek(serviceType, fromDate, "RAG", null);
                llmApiStats = getStatThisWeek(serviceType, fromDate, "LLM", "API");
                embedApiStats = getStatThisWeek(serviceType, fromDate, "RAG", "API");
                break;
            case("MONTH"):
                log.debug("이번달");
                llmTokenStats = getStatThisMonth(serviceType, fromDate, "LLM", null);
                embedTokenStats = getStatThisMonth(serviceType, fromDate, "RAG", null);
                llmApiStats = getStatThisMonth(serviceType, fromDate, "LLM", "API");
                embedApiStats = getStatThisMonth(serviceType, fromDate, "RAG", "API");
                break;
            case("PERIOD"):
                log.debug("기간");
                if(fromDate == null || fromDate.isEmpty() || toDate == null || toDate.isEmpty())
                    return BaseResponse.failure(stats, ResponseMeta.PARAM_WRONG);
                llmTokenStats = getStatPeriod(serviceType, fromDate, toDate, "LLM", null);
                embedTokenStats = getStatPeriod(serviceType, fromDate, toDate, "RAG", null);
                llmApiStats = getStatPeriod(serviceType, fromDate, toDate, "LLM", "API");
                embedApiStats = getStatPeriod(serviceType, fromDate, toDate, "RAG", "API");
                break;
            default:
                log.debug("???");
                break;
        }

        stats.setLlmStats(new StatBoards(llmApiStats, llmTokenStats));
        stats.setEmbedStats(new StatBoards(embedApiStats, embedTokenStats));

        return BaseResponse.success(stats);
    }

    private String getDataType(String serviceType, String dataType) {
        String rtn = dataType;
        if("RANKER".equals(serviceType)) {
            if("LLM".equals(dataType))
                rtn = "RNK_LLM";
            else if("RAG".equals(dataType))
                rtn = "RNK_EMBED";
        }
        return rtn;
    }

    private StatBoard getStatToday(String serviceType, String fromDate, String dataType, String statType) {
        StatBoard todayStat = null;
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("fromDate", fromDate==null||fromDate.isEmpty() ? DateUtil.getTodayString():fromDate);
            param.put("dataType", getDataType(serviceType, dataType));
            param.put("isDaily", true);
            String result = null;
            if("CHATHUB".equals(serviceType)) {
                if("API".equals(statType))
                    result = statisticsMapper.apiStatDay(param);
                else
                    result = statisticsMapper.statDay(param);
            } else {
                if("API".equals(statType))
//                    result = statisticsMapper.apiStatDayRanker(param);
                    result = statisticsMapper.apiStatDayRanker(param);
                else
                    result = statisticsMapper.statDayRanker(param);
            }
            todayStat = ObjectMapperUtil.readValue(result, StatBoard.class);
            return todayStat;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw BaseException.of(ResponseMeta.FAILURE);
        }
    }

    private StatBoard getStatThisWeek(String serviceType, String fromDate, String dataType, String statType) {
        StatBoard thisWeekStat = null;
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("fromDate", fromDate==null||fromDate.isEmpty() ? DateUtil.getTodayString():fromDate);
            param.put("dataType", getDataType(serviceType, dataType));
            String result = null;
            if("CHATHUB".equals(serviceType)) {
                if("API".equals(statType))
                    result = statisticsMapper.apiStatWeek(param);
                else
                    result = statisticsMapper.statWeek(param);
            } else {
                if("API".equals(statType))
                    result = statisticsMapper.apiStatWeekRanker(param);
                else
                    result = statisticsMapper.statWeekRanker(param);
            }
            thisWeekStat = ObjectMapperUtil.readValue(result, StatBoard.class);
            return thisWeekStat;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw BaseException.of(ResponseMeta.FAILURE);
        }
    }

    private StatBoard getStatThisMonth(String serviceType, String fromDate, String dataType, String statType) {
        StatBoard thisMonthStat = null;
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("fromDate", fromDate==null||fromDate.isEmpty() ? DateUtil.getTodayString():fromDate);
            param.put("dataType", getDataType(serviceType, dataType));
            String result = null;
            if("CHATHUB".equals(serviceType)) {
                if("API".equals(statType))
                    result = statisticsMapper.apiStatMonth(param);
                else
                    result = statisticsMapper.statMonth(param);
            } else {
                if("API".equals(statType))
                    result = statisticsMapper.apiStatMonthRanker(param);
                else
                    result = statisticsMapper.statMonthRanker(param);
            }
            thisMonthStat = ObjectMapperUtil.readValue(result, StatBoard.class);
            return thisMonthStat;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw BaseException.of(ResponseMeta.FAILURE);
        }
    }

    private StatBoard getStatPeriod(String serviceType, String fromDate, String toDate, String dataType, String statType) {
        StatBoard periodStat = null;
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("fromDate", fromDate);
            param.put("toDate", toDate);
            param.put("dataType", getDataType(serviceType, dataType));

            String result = null;
            if("CHATHUB".equals(serviceType)) {
                if("API".equals(statType))
                    result = statisticsMapper.apiStatPeriod(param);
                else
                    result = statisticsMapper.statPeriod(param);
            } else {
                if("API".equals(statType))
                    result = statisticsMapper.apiStatPeriodRanker(param);
                else
                    result = statisticsMapper.statPeriodRanker(param);
            }
            periodStat = ObjectMapperUtil.readValue(result, StatBoard.class);
            return periodStat;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw BaseException.of(ResponseMeta.FAILURE);
        }
    }
}
