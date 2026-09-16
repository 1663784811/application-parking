package com.cyyaw.admin.application.parking.service.impl;

import com.cyyaw.admin.application.parking.service.DashboardService;
import com.cyyaw.admin.entity.dto.parking.DashboardStatsVO;
import com.cyyaw.admin.dao.dashboard.DashboardDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardDao dashboardDao;

    @Override
    public DashboardStatsVO getStats() {
        Map<String, Object> m = dashboardDao.selectStats();
        if (m == null) {
            m = new LinkedHashMap<>();
        }
        DashboardStatsVO vo = new DashboardStatsVO();

        BigDecimal today = toBigDecimal(m.get("todayRevenue"));
        BigDecimal yesterday = toBigDecimal(m.get("yesterdayRevenue"));
        vo.setTodayRevenue(today);
        vo.setYesterdayRevenue(yesterday);

        // 营收同比（%）= (today - yesterday) / yesterday * 100；昨日为 0 时记 0 避免除零
        BigDecimal trend = BigDecimal.ZERO;
        if (yesterday.compareTo(BigDecimal.ZERO) != 0) {
            trend = today.subtract(yesterday)
                    .divide(yesterday, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        vo.setRevenueTrend(trend);

        int currentVehicles = toInt(m.get("currentVehicles"));
        int totalSpaces = toInt(m.get("totalSpaces"));
        vo.setCurrentVehicles(currentVehicles);
        vo.setTotalSpaces(totalSpaces);
        vo.setSpaceUsageRate(totalSpaces == 0 ? 0 : currentVehicles * 100 / totalSpaces);

        vo.setTodayIn(toInt(m.get("todayIn")));
        vo.setTodayOut(toInt(m.get("todayOut")));

        int unpaid = toInt(m.get("unpaidCount"));
        vo.setUnpaidCount(unpaid);
        vo.setExceptionCount(unpaid); // 待处理异常订单 ≈ 未支付订单

        vo.setNoPlateCount(toInt(m.get("noPlateCount")));
        vo.setFaultCount(0); // 设备故障统计待对接设备模块
        return vo;
    }

    @Override
    public List<Map<String, Object>> getRevenueTrend() {
        List<Map<String, Object>> rows = dashboardDao.selectRevenueTrend();
        Map<String, Object> byDate = new LinkedHashMap<>();
        if (rows != null) {
            for (Map<String, Object> r : rows) {
                byDate.put((String) r.get("date"), r.get("amount"));
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String d = LocalDate.now().minusDays(i).toString();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", d);
            item.put("amount", toBigDecimal(byDate.get(d)));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getTrafficTrend() {
        List<Map<String, Object>> inRows = dashboardDao.selectInTrend();
        List<Map<String, Object>> outRows = dashboardDao.selectOutTrend();
        Map<String, Object> inMap = new LinkedHashMap<>();
        Map<String, Object> outMap = new LinkedHashMap<>();
        if (inRows != null) {
            for (Map<String, Object> r : inRows) {
                inMap.put((String) r.get("date"), r.get("cnt"));
            }
        }
        if (outRows != null) {
            for (Map<String, Object> r : outRows) {
                outMap.put((String) r.get("date"), r.get("cnt"));
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String d = LocalDate.now().minusDays(i).toString();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", d);
            item.put("inCount", toInt(inMap.get(d)));
            item.put("outCount", toInt(outMap.get(d)));
            result.add(item);
        }
        return result;
    }

    private static BigDecimal toBigDecimal(Object v) {
        if (v == null) {
            return BigDecimal.ZERO;
        }
        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
        }
        try {
            return new BigDecimal(v.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private static int toInt(Object v) {
        if (v == null) {
            return 0;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
