package com.cyyaw.admin.application.parking.report.service.impl;

import com.cyyaw.admin.application.parking.report.service.SpaceUsageReportService;
import com.cyyaw.admin.application.parking.report.vo.SpaceUsageReportVO;
import com.cyyaw.admin.dao.report.ReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpaceUsageReportServiceImpl implements SpaceUsageReportService {

    @Autowired
    private ReportDao reportDao;

    @Override
    public SpaceUsageReportVO getStats(Long parkingId) {
        Map<String, Object> m = reportDao.selectSpaceStats(parkingId);
        if (m == null) {
            m = new LinkedHashMap<>();
        }
        int totalSpaces = toInt(m.get("totalSpaces"));
        int currentIn = toInt(m.get("currentIn"));

        // 分小时在场峰值（用于高峰占用率/高峰时段）
        List<Map<String, Object>> hourly = reportDao.selectHourlyOccupancy(parkingId);
        int peakInLot = 0;
        int peakHour = -1;
        if (hourly != null) {
            for (Map<String, Object> r : hourly) {
                int cnt = toInt(r.get("inLot"));
                if (cnt > peakInLot) {
                    peakInLot = cnt;
                    peakHour = toInt(r.get("hour"));
                }
            }
        }

        SpaceUsageReportVO vo = new SpaceUsageReportVO();
        vo.setTotalSpaces(totalSpaces);
        vo.setCurrentIn(currentIn);
        // 空置率 = (1 - 当前在场 / 总车位) * 100，下限 0
        int vacancy = totalSpaces > 0
                ? Math.max(0, Math.round((1f - (float) currentIn / totalSpaces) * 100))
                : 100;
        vo.setVacancyRate(vacancy);
        // 高峰占用率 = 峰值在场 / 总车位 * 100
        vo.setPeakRate(totalSpaces > 0 ? Math.round((float) peakInLot * 100 / totalSpaces) : 0);
        vo.setPeakHour(peakHour >= 0 ? String.format("%02d:00-%02d:00", peakHour, (peakHour + 1) % 24) : "无");
        return vo;
    }

    @Override
    public List<Map<String, Object>> getHourly(Long parkingId) {
        List<Map<String, Object>> rows = reportDao.selectHourlyOccupancy(parkingId);
        int[] inLot = new int[24];
        if (rows != null) {
            for (Map<String, Object> r : rows) {
                inLot[toInt(r.get("hour"))] = toInt(r.get("inLot"));
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("hour", h);
            item.put("inLot", inLot[h]);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getArea(Long parkingId) {
        List<Map<String, Object>> rows = reportDao.selectAreaBreakdown(parkingId);
        List<Map<String, Object>> result = new ArrayList<>();
        if (rows != null) {
            for (Map<String, Object> r : rows) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("area", r.get("area"));
                item.put("total", toInt(r.get("total")));
                item.put("occupied", toInt(r.get("occupied")));
                result.add(item);
            }
        }
        return result;
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
