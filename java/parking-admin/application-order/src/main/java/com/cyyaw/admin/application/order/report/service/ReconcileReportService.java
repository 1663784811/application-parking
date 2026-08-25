package com.cyyaw.admin.application.order.report.service;

import com.cyyaw.admin.application.order.report.vo.ReconcileReportVO;

import java.util.List;
import java.util.Map;

public interface ReconcileReportService {

    /** 对账汇总（总/线上/线下订单数与金额、差异） */
    ReconcileReportVO getStats(String start, String end, Long parkingId);

    /** 对账明细（按停车场+日聚合，分页） */
    List<Map<String, Object>> getDetailList(String start, String end, Long parkingId, int offset, int size);

    /** 对账明细分组总数 */
    long getDetailCount(String start, String end, Long parkingId);
}
