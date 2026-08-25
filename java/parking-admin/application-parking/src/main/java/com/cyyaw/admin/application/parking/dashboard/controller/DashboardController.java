package com.cyyaw.admin.application.parking.dashboard.controller;

import com.cyyaw.admin.application.parking.dashboard.service.DashboardService;
import com.cyyaw.admin.application.parking.dashboard.vo.DashboardStatsVO;
import com.cyyaw.admin.common.BaseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "工作台")
@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "核心指标", description = "今日营收、在场车辆、进出车次、异常订单等")
    @GetMapping("/stats")
    public BaseResult<DashboardStatsVO> stats() {
        return BaseResult.ok(dashboardService.getStats());
    }

    @Operation(summary = "近7日营收趋势", description = "返回 { date(yyyy-MM-dd), amount } 列表")
    @GetMapping("/revenueTrend")
    public BaseResult<List<Map<String, Object>>> revenueTrend() {
        return BaseResult.ok(dashboardService.getRevenueTrend());
    }

    @Operation(summary = "近7日车流量", description = "返回 { date(yyyy-MM-dd), inCount, outCount } 列表")
    @GetMapping("/trafficTrend")
    public BaseResult<List<Map<String, Object>>> trafficTrend() {
        return BaseResult.ok(dashboardService.getTrafficTrend());
    }
}
