package com.cyyaw.admin.application.parking.report.service;

import com.cyyaw.admin.application.parking.report.vo.MemberRevenueReportVO;

import java.util.List;
import java.util.Map;

public interface MemberRevenueReportService {

    /**
     * 月卡营收汇总：续费总金额 / 续费笔数 / 活跃会员数 / 人均 / 峰值月份
     */
    MemberRevenueReportVO getStats(String start, String end, Integer cardType);

    /**
     * 月卡营收按月趋势：{ month(yyyy-MM), revenue, count }
     */
    List<Map<String, Object>> getMonthlyTrend(String start, String end, Integer cardType);

    /**
     * 会员营收明细（按会员聚合，分页）：{ memberId, name, plate, cardType, renewalCount, totalAmount }
     */
    List<Map<String, Object>> getDetail(String start, String end, Integer cardType, int offset, int size);

    /**
     * 会员营收明细按会员分组总数（分页 total）
     */
    long getDetailCount(String start, String end, Integer cardType);
}
