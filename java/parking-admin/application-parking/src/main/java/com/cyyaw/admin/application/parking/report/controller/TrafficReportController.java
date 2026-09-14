package com.cyyaw.admin.application.parking.report.controller;

import com.cyyaw.admin.application.parking.report.service.TrafficReportService;
import com.cyyaw.admin.application.parking.report.vo.TrafficReportVO;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.parking.TrafficReportDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "车流量报表")
@RestController
@RequestMapping("/admin/report/traffic")
public class TrafficReportController {

    @Autowired
    private TrafficReportService trafficReportService;

    @Operation(summary = "车流量汇总", description = "今日入场/出场、当前在场、峰值时段；以 TrafficReportDTO 接收查询参数")
    @GetMapping("/stats")
    public BaseResult<TrafficReportVO> stats(TrafficReportDTO query) {
        return BaseResult.ok(trafficReportService.getStats(query.getParkingId()));
    }

    @Operation(summary = "今日分时段车流量", description = "返回 { hour(0-23), inCount, outCount } 列表；以 TrafficReportDTO 接收")
    @GetMapping("/hourly")
    public BaseResult<List<Map<String, Object>>> hourly(TrafficReportDTO query) {
        return BaseResult.ok(trafficReportService.getHourly(query.getParkingId()));
    }

    @Operation(summary = "近7日车流量", description = "返回 { date(yyyy-MM-dd), inCount, outCount } 列表；以 TrafficReportDTO 接收")
    @GetMapping("/daily")
    public BaseResult<List<Map<String, Object>>> daily(TrafficReportDTO query) {
        return BaseResult.ok(trafficReportService.getDaily(query.getParkingId()));
    }

    @Operation(summary = "今日2小时分段明细", description = "返回 { period, inCount, outCount, inPark, inPeak, outPeak } 列表；以 TrafficReportDTO 接收")
    @GetMapping("/table")
    public BaseResult<List<Map<String, Object>>> table(TrafficReportDTO query) {
        return BaseResult.ok(trafficReportService.getPeriodTable(query.getParkingId()));
    }
}
