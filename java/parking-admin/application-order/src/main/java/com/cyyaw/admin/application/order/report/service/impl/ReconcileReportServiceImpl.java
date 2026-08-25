package com.cyyaw.admin.application.order.report.service.impl;

import com.cyyaw.admin.application.order.report.service.ReconcileReportService;
import com.cyyaw.admin.application.order.report.vo.ReconcileReportVO;
import com.cyyaw.admin.dao.report.ReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class ReconcileReportServiceImpl implements ReconcileReportService {

    @Autowired
    private ReportDao reportDao;

    @Override
    public ReconcileReportVO getStats(String start, String end, Long parkingId) {
        ReconcileReportVO vo = new ReconcileReportVO();
        Map<String, Object> m = reportDao.selectReconcileStats(start, end, parkingId);

        BigDecimal totalRevenue = sum(m, "totalRevenue");
        BigDecimal onlineRevenue = sum(m, "onlineRevenue");
        BigDecimal offlineRevenue = sum(m, "offlineRevenue");
        // 差异 = 总额 - 线上 - 线下（未归入 1-4 类的支付方式金额；全为 1-4 类时为 0）
        BigDecimal difference = totalRevenue.subtract(onlineRevenue).subtract(offlineRevenue);

        vo.setTotalRevenue(totalRevenue);
        vo.setTotalOrders(toInt(m.get("totalOrders")));
        vo.setOnlineRevenue(onlineRevenue);
        vo.setOnlineCount(toInt(m.get("onlineCount")));
        vo.setOfflineRevenue(offlineRevenue);
        vo.setOfflineCount(toInt(m.get("offlineCount")));
        vo.setDifference(difference);
        return vo;
    }

    @Override
    public List<Map<String, Object>> getDetailList(String start, String end, Long parkingId, int offset, int size) {
        return reportDao.selectReconcileDetail(start, end, parkingId, offset, size);
    }

    @Override
    public long getDetailCount(String start, String end, Long parkingId) {
        return reportDao.selectReconcileDetailCount(start, end, parkingId);
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
