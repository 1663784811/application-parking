package com.cyyaw.admin.application.order.report.controller;

import com.cyyaw.admin.application.order.report.service.RevenueReportService;
import com.cyyaw.admin.application.order.report.vo.RevenueReportVO;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.order.RevenueReportDTO;
import com.cyyaw.admin.entity.dto.order.RevenueReportDetailDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "营收报表")
@RestController
@RequestMapping("/admin/report/revenue")
public class RevenueReportController {

    @Autowired
    private RevenueReportService revenueReportService;

    @Operation(summary = "营收汇总", description = "线上/线下/优惠/订单数/同比；以 RevenueReportDTO 接收查询参数")
    @GetMapping("/stats")
    public BaseResult<RevenueReportVO> stats(RevenueReportDTO query) {
        return BaseResult.ok(revenueReportService.getStats(query.getStart(), query.getEnd(), query.getParkingId()));
    }

    @Operation(summary = "日营收趋势", description = "返回 { date(yyyy-MM-dd), amount } 列表；以 RevenueReportDTO 接收查询参数")
    @GetMapping("/daily")
    public BaseResult<List<Map<String, Object>>> daily(RevenueReportDTO query) {
        return BaseResult.ok(revenueReportService.getDailyRevenue(query.getStart(), query.getEnd(), query.getParkingId()));
    }

    @Operation(summary = "营收明细（分页）", description = "按日返回 { date, onlineAmount, offlineAmount, discountAmount, totalAmount, orderCount }；以 RevenueReportDetailDTO 接收查询参数")
    @GetMapping("/detail")
    public BaseResult<List<Map<String, Object>>> detail(RevenueReportDetailDTO query) {
        String start = query.getStart();
        String end = query.getEnd();
        Long parkingId = query.getParkingId();
        Integer page = query.getPage();
        Integer size = query.getSize();
        int offset = (page - 1) * size;
        List<Map<String, Object>> list = revenueReportService.getDetailList(start, end, parkingId, offset, size);
        long total = revenueReportService.getDetailCount(start, end, parkingId);
        BaseResult.Result result = new BaseResult.Result(page, size, total);
        return BaseResult.ok(list, result);
    }
}
