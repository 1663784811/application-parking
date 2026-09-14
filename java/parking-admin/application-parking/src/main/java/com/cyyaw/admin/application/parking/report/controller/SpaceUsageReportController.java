package com.cyyaw.admin.application.parking.report.controller;

import com.cyyaw.admin.application.parking.report.service.SpaceUsageReportService;
import com.cyyaw.admin.application.parking.report.vo.SpaceUsageReportVO;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.parking.SpaceUsageReportDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "车位利用率报表")
@RestController
@RequestMapping("/admin/report/space")
public class SpaceUsageReportController {

    @Autowired
    private SpaceUsageReportService spaceUsageReportService;

    @Operation(summary = "车位利用率汇总", description = "总车位/当前占用/空置率/高峰占用率/高峰时段；以 SpaceUsageReportDTO 接收查询参数")
    @GetMapping("/stats")
    public BaseResult<SpaceUsageReportVO> stats(SpaceUsageReportDTO query) {
        return BaseResult.ok(spaceUsageReportService.getStats(query.getParkingId()));
    }

    @Operation(summary = "今日分时段在场车辆", description = "返回 { hour(0-23), inLot } 列表；以 SpaceUsageReportDTO 接收")
    @GetMapping("/hourly")
    public BaseResult<List<Map<String, Object>>> hourly(SpaceUsageReportDTO query) {
        return BaseResult.ok(spaceUsageReportService.getHourly(query.getParkingId()));
    }

    @Operation(summary = "区域占用对比", description = "返回 { area, total, occupied } 列表；以 SpaceUsageReportDTO 接收")
    @GetMapping("/area")
    public BaseResult<List<Map<String, Object>>> area(SpaceUsageReportDTO query) {
        return BaseResult.ok(spaceUsageReportService.getArea(query.getParkingId()));
    }
}
