package com.cyyaw.admin.application.parking.service;

import com.cyyaw.admin.entity.dto.parking.TrafficReportVO;

import java.util.List;
import java.util.Map;

/**
 * 车流量报表服务（基于 pk_car_log：entry_time 入场 / out_time 出场 / status=0 在场）
 */
public interface TrafficReportService {

    /** 车流量汇总：今日入场/出场、当前在场、峰值时段 */
    TrafficReportVO getStats(Long parkingId);

    /** 今日分小时车流量：{ hour(0-23), inCount, outCount }（已补齐 0-23） */
    List<Map<String, Object>> getHourly(Long parkingId);

    /** 近7日车流量：{ date(yyyy-MM-dd), inCount, outCount }（已补齐7日） */
    List<Map<String, Object>> getDaily(Long parkingId);

    /** 今日 2 小时分段明细：{ period, inCount, outCount, inPark, inPeak, outPeak } */
    List<Map<String, Object>> getPeriodTable(Long parkingId);
}
