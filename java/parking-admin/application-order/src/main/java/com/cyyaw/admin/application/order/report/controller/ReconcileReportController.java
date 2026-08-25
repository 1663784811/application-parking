package com.cyyaw.admin.application.order.report.controller;

import com.cyyaw.admin.application.order.report.service.ReconcileReportService;
import com.cyyaw.admin.application.order.report.vo.ReconcileReportVO;
import com.cyyaw.admin.common.BaseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "订单对账")
@RestController
@RequestMapping("/admin/report/reconcile")
public class ReconcileReportController {

    @Autowired
    private ReconcileReportService reconcileReportService;

    @Operation(summary = "对账汇总", description = "总/线上/线下营收与订单数、差异金额")
    @GetMapping("/stats")
    public BaseResult<ReconcileReportVO> stats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) Long parkingId) {
        return BaseResult.ok(reconcileReportService.getStats(start, end, parkingId));
    }

    @Operation(summary = "对账明细（分页）", description = "按停车场+日返回 { parkingId, parkingName, reconcileDate, onlineOrders, onlineAmount, offlineOrders, offlineAmount, totalAmount, diffAmount }")
    @GetMapping("/detail")
    public BaseResult<List<Map<String, Object>>> detail(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) Long parkingId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        int offset = (page - 1) * size;
        List<Map<String, Object>> list = reconcileReportService.getDetailList(start, end, parkingId, offset, size);
        long total = reconcileReportService.getDetailCount(start, end, parkingId);
        BaseResult.Result result = new BaseResult.Result(page, size, total);
        return BaseResult.ok(list, result);
    }
}
