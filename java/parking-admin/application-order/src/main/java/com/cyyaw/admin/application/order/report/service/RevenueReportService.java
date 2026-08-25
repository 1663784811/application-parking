package com.cyyaw.admin.application.order.report.service;

import com.cyyaw.admin.application.order.report.vo.RevenueReportVO;

import java.util.List;
import java.util.Map;

public interface RevenueReportService {

    /** 营收汇总（含线上/线下占比与同比） */
    RevenueReportVO getStats(String start, String end, Long parkingId);

    /** 日营收趋势：{ date(yyyy-MM-dd), amount } */
    List<Map<String, Object>> getDailyRevenue(String start, String end, Long parkingId);

    /** 营收明细（按日，分页） */
    List<Map<String, Object>> getDetailList(String start, String end, Long parkingId, int offset, int size);

    /** 营收明细按日分组总数 */
    long getDetailCount(String start, String end, Long parkingId);
}
