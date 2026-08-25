package com.cyyaw.admin.application.order.report.service.impl;

import com.cyyaw.admin.application.order.report.service.RevenueReportService;
import com.cyyaw.admin.application.order.report.vo.RevenueReportVO;
import com.cyyaw.admin.dao.report.ReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class RevenueReportServiceImpl implements RevenueReportService {

    @Autowired
    private ReportDao reportDao;

    @Override
    public RevenueReportVO getStats(String start, String end, Long parkingId) {
        RevenueReportVO vo = new RevenueReportVO();

        // 本周期
        Map<String, Object> cur = reportDao.selectRevenueStats(start, end, parkingId);
        BigDecimal online = sum(cur, "onlineRevenue");
        BigDecimal offline = sum(cur, "offlineRevenue");
        BigDecimal total = online.add(offline);
        BigDecimal discount = sum(cur, "discount");
        int orderCount = toInt(cur.get("orderCount"));

        vo.setOnlineRevenue(online);
        vo.setOfflineRevenue(offline);
        vo.setTotalRevenue(total);
        vo.setDiscount(discount);
        vo.setOrderCount(orderCount);
        vo.setOnlinePercent(percent(online, total));
        vo.setOfflinePercent(percent(offline, total));

        // 上一等长周期同比：(total - priorTotal) / priorTotal * 100
        LocalDate s = LocalDate.parse(start);
        LocalDate e = LocalDate.parse(end);
        long days = ChronoUnit.DAYS.between(s, e) + 1;       // 含首尾天数
        LocalDate priorEnd = s.minusDays(1);                   // 上一周期结束日（含）
        LocalDate priorStart = priorEnd.minusDays(days - 1);   // 上一周期开始日
        Map<String, Object> prior = reportDao.selectRevenueStats(priorStart.toString(), priorEnd.toString(), parkingId);
        BigDecimal priorTotal = sum(prior, "onlineRevenue").add(sum(prior, "offlineRevenue"));
        BigDecimal trend = BigDecimal.ZERO;
        if (priorTotal.signum() != 0) {
            trend = total.subtract(priorTotal)
                    .divide(priorTotal, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        vo.setRevenueTrend(trend);
        return vo;
    }

    @Override
    public List<Map<String, Object>> getDailyRevenue(String start, String end, Long parkingId) {
        return reportDao.selectDailyRevenue(start, end, parkingId);
    }

    @Override
    public List<Map<String, Object>> getDetailList(String start, String end, Long parkingId, int offset, int size) {
        return reportDao.selectRevenueDetail(start, end, parkingId, offset, size);
    }

    @Override
    public long getDetailCount(String start, String end, Long parkingId) {
        return reportDao.selectRevenueDetailCount(start, end, parkingId);
    }

    /** 占比（0~100，四舍五入取整），分母为 0 时记 0 */
    private static int percent(BigDecimal part, BigDecimal total) {
        if (total == null || total.signum() == 0) {
            return 0;
        }
        return part.multiply(new BigDecimal("100")).divide(total, 0, RoundingMode.HALF_UP).intValue();
    }

    private static BigDecimal sum(Map<String, Object> m, String key) {
        if (m == null) {
            return BigDecimal.ZERO;
        }
        Object v = m.get(key);
        if (v == null) {
            return BigDecimal.ZERO;
        }
        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
        }
        if (v instanceof Number) {
            return new BigDecimal(v.toString());
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
