package com.cyyaw.admin.application.parking.report.service.impl;

import com.cyyaw.admin.application.parking.report.service.MemberRevenueReportService;
import com.cyyaw.admin.application.parking.report.vo.MemberRevenueReportVO;
import com.cyyaw.admin.dao.report.ReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class MemberRevenueReportServiceImpl implements MemberRevenueReportService {

    @Autowired
    private ReportDao reportDao;

    @Override
    public MemberRevenueReportVO getStats(String start, String end, Integer cardType) {
        Map<String, Object> m = reportDao.selectMemberRevenueStats(start, end, cardType);
        BigDecimal totalRevenue = sum(m, "totalRevenue");
        int renewalCount = toInt(m == null ? null : m.get("renewalCount"));
        int memberCount = toInt(m == null ? null : m.get("memberCount"));

        MemberRevenueReportVO vo = new MemberRevenueReportVO();
        vo.setTotalRevenue(totalRevenue);
        vo.setRenewalCount(renewalCount);
        vo.setMemberCount(memberCount);
        // 人均续费金额 = 总金额 / 会员数
        vo.setAvgRevenue(memberCount > 0
                ? totalRevenue.divide(new BigDecimal(memberCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        // 月度峰值（峰值月份 / 峰值金额）：扫描月度趋势取最大
        List<Map<String, Object>> monthly = reportDao.selectMemberRevenueMonthly(start, end, cardType);
        BigDecimal peakRevenue = BigDecimal.ZERO;
        String peakMonth = "无";
        if (monthly != null) {
            for (Map<String, Object> r : monthly) {
                BigDecimal rev = sum(r, "revenue");
                if (rev.compareTo(peakRevenue) > 0) {
                    peakRevenue = rev;
                    peakMonth = r.get("month") == null ? "无" : r.get("month").toString();
                }
            }
        }
        vo.setPeakMonth(peakMonth);
        vo.setPeakRevenue(peakRevenue);
        return vo;
    }

    @Override
    public List<Map<String, Object>> getMonthlyTrend(String start, String end, Integer cardType) {
        return reportDao.selectMemberRevenueMonthly(start, end, cardType);
    }

    @Override
    public List<Map<String, Object>> getDetail(String start, String end, Integer cardType, int offset, int size) {
        return reportDao.selectMemberRevenueDetail(start, end, cardType, offset, size);
    }

    @Override
    public long getDetailCount(String start, String end, Integer cardType) {
        return reportDao.selectMemberRevenueDetailCount(start, end, cardType);
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
