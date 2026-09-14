package com.cyyaw.admin.application.parking.report.controller;

import com.cyyaw.admin.application.parking.report.service.MemberRevenueReportService;
import com.cyyaw.admin.application.parking.report.vo.MemberRevenueReportVO;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.parking.MemberRevenueReportDTO;
import com.cyyaw.admin.entity.dto.parking.MemberRevenueReportDetailDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "月卡营收报表")
@RestController
@RequestMapping("/admin/report/memberRevenue")
public class MemberRevenueReportController {

    @Autowired
    private MemberRevenueReportService memberRevenueReportService;

    @Operation(summary = "月卡营收汇总", description = "续费总金额/续费笔数/活跃会员数/人均/峰值月份；以 MemberRevenueReportDTO 接收查询参数")
    @GetMapping("/stats")
    public BaseResult<MemberRevenueReportVO> stats(MemberRevenueReportDTO query) {
        return BaseResult.ok(memberRevenueReportService.getStats(query.getStart(), query.getEnd(), query.getCardType()));
    }

    @Operation(summary = "月卡营收趋势", description = "按月返回 { month(yyyy-MM), revenue, count } 列表；以 MemberRevenueReportDTO 接收")
    @GetMapping("/trend")
    public BaseResult<List<Map<String, Object>>> trend(MemberRevenueReportDTO query) {
        return BaseResult.ok(memberRevenueReportService.getMonthlyTrend(query.getStart(), query.getEnd(), query.getCardType()));
    }

    @Operation(summary = "会员营收明细（分页）", description = "按会员聚合返回 { memberId, name, plate, cardType, renewalCount, totalAmount }；以 MemberRevenueReportDetailDTO 接收")
    @GetMapping("/detail")
    public BaseResult<List<Map<String, Object>>> detail(MemberRevenueReportDetailDTO query) {
        String start = query.getStart();
        String end = query.getEnd();
        Integer cardType = query.getCardType();
        Integer page = query.getPage();
        Integer size = query.getSize();
        int offset = (page - 1) * size;
        List<Map<String, Object>> list = memberRevenueReportService.getDetail(start, end, cardType, offset, size);
        long total = memberRevenueReportService.getDetailCount(start, end, cardType);
        BaseResult.Result result = new BaseResult.Result(page, size, total);
        return BaseResult.ok(list, result);
    }
}
