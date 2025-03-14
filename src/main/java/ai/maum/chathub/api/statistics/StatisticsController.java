package ai.maum.chathub.api.statistics;

import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.statistics.dto.res.ChathubStatsRes;
import ai.maum.chathub.api.statistics.model.*;
import ai.maum.chathub.api.statistics.service.StatisticsService;
import ai.maum.chathub.meta.ResponseMeta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name="통계", description="통계")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @Operation(summary = "챗허브 통계", description = "챗허브 통계")
    @ResponseBody
    @GetMapping({"/statistics/chathub"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<ChathubStatsRes> getChathubStatistics(
             @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
            ,@Parameter(name="type", description = "서치타입: DAY(오늘), WEEK(금주), MONTH(이번달), PERIOD(기간)") @RequestParam(value = "type", required = false) String statType
            ,@Parameter(name="from_date", description = "YYYY-MM-DD") @RequestParam(value = "from_date", required = false) String fromDate
            ,@Parameter(name="to_date", description = "YYYY-MM-DD") @RequestParam(value = "to_date", required = false) String toDate
    ) {

        if(statType == null)
            return BaseResponse.failure(new ChathubStatsRes(), ResponseMeta.PARAM_WRONG);

        return statisticsService.getStats("CHATHUB", fromDate, toDate, statType);
    }

    @Operation(summary = "랭커 통계", description = "랭커 통계")
    @ResponseBody
    @GetMapping({"/statistics/ranker"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<ChathubStatsRes> getRankerStatistics(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
            ,@Parameter(name="type", description = "서치타입: DAY(오늘), WEEK(금주), MONTH(이번달), PERIOD(기간)") @RequestParam(value = "type", required = false) String statType
            ,@Parameter(name="from_date", description = "YYYY-MM-DD") @RequestParam(value = "from_date", required = false) String fromDate
            ,@Parameter(name="to_date", description = "YYYY-MM-DD") @RequestParam(value = "to_date", required = false) String toDate
    ) {

        if(statType == null)
            return BaseResponse.failure(new ChathubStatsRes(), ResponseMeta.PARAM_WRONG);

        return statisticsService.getStats("RANKER", fromDate, toDate, statType);
    }
}