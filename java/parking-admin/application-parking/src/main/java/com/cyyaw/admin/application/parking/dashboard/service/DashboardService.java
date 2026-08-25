package com.cyyaw.admin.application.parking.dashboard.service;

import com.cyyaw.admin.application.parking.dashboard.vo.DashboardStatsVO;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    /** 核心指标 */
    DashboardStatsVO getStats();

    /** 近7日营收趋势：{ date(yyyy-MM-dd), amount } */
    List<Map<String, Object>> getRevenueTrend();

    /** 近7日车流量：{ date(yyyy-MM-dd), inCount, outCount } */
    List<Map<String, Object>> getTrafficTrend();
}
