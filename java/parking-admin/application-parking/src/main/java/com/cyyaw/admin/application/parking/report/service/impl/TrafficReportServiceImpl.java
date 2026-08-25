package com.cyyaw.admin.application.parking.report.service.impl;

import com.cyyaw.admin.application.parking.report.service.TrafficReportService;
import com.cyyaw.admin.application.parking.report.vo.TrafficReportVO;
import com.cyyaw.admin.dao.report.ReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrafficReportServiceImpl implements TrafficReportService {

    @Autowired
    private ReportDao reportDao;

    @Override
    public TrafficReportVO getStats(Long parkingId) {
        Map<String, Object> m = reportDao.selectTrafficStats(parkingId);
        if (m == null) {
            m = new LinkedHashMap<>();
        }
        TrafficReportVO vo = new TrafficReportVO();
        vo.setTodayIn(toInt(m.get("todayIn")));
        vo.setTodayOut(toInt(m.get("todayOut")));
        vo.setCurrentIn(toInt(m.get("currentIn")));

        // 入场峰值时段：当日无入场时子查询返回 null，记 "无"
        Object peakObj = m.get("peakHour");
        String peakHour;
        if (peakObj == null) {
            peakHour = "无";
        } else {
            int peak = toInt(peakObj);
            peakHour = String.format("%02d:00-%02d:00", peak, (peak + 1) % 24);
        }
        vo.setPeakHour(peakHour);
        return vo;
    }

    @Override
    public List<Map<String, Object>> getHourly(Long parkingId) {
        List<Map<String, Object>> inRows = reportDao.selectHourlyIn(parkingId);
        List<Map<String, Object>> outRows = reportDao.selectHourlyOut(parkingId);
        int[] inArr = new int[24];
        int[] outArr = new int[24];
        if (inRows != null) {
            for (Map<String, Object> r : inRows) {
                inArr[toInt(r.get("hour"))] = toInt(r.get("cnt"));
            }
        }
        if (outRows != null) {
            for (Map<String, Object> r : outRows) {
                outArr[toInt(r.get("hour"))] = toInt(r.get("cnt"));
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("hour", h);
            item.put("inCount", inArr[h]);
            item.put("outCount", outArr[h]);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getDaily(Long parkingId) {
        List<Map<String, Object>> inRows = reportDao.selectDailyIn(parkingId);
        List<Map<String, Object>> outRows = reportDao.selectDailyOut(parkingId);
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

    @Override
    public List<Map<String, Object>> getPeriodTable(Long parkingId) {
        // 由今日分小时数据聚合为 12 个 2 小时分段
        List<Map<String, Object>> hourly = getHourly(parkingId);
        int[] inArr = new int[24];
        int[] outArr = new int[24];
        for (int h = 0; h < 24; h++) {
            inArr[h] = toInt(hourly.get(h).get("inCount"));
            outArr[h] = toInt(hourly.get(h).get("outCount"));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        int cumIn = 0;
        int cumOut = 0;
        for (int b = 0; b < 12; b++) {
            int h0 = b * 2;
            int h1 = h0 + 1;
            int inSum = inArr[h0] + inArr[h1];
            int outSum = outArr[h0] + outArr[h1];
            int inPeak = Math.max(inArr[h0], inArr[h1]);
            int outPeak = Math.max(outArr[h0], outArr[h1]);
            cumIn += inSum;
            cumOut += outSum;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("period", String.format("%02d:00-%02d:00", h0, h0 + 2));
            item.put("inCount", inSum);
            item.put("outCount", outSum);
            // 在场 = 截至该时段末的净在场（今日累计入场 - 出场）
            item.put("inPark", Math.max(0, cumIn - cumOut));
            item.put("inPeak", inPeak);
            item.put("outPeak", outPeak);
            result.add(item);
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
