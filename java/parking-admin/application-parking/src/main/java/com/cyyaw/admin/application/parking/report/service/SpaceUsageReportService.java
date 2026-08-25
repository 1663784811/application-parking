package com.cyyaw.admin.application.parking.report.service;

import com.cyyaw.admin.application.parking.report.vo.SpaceUsageReportVO;

import java.util.List;
import java.util.Map;

/**
 * 车位利用率报表服务（基于 pk_space 总车位 + pk_car_log 在场车辆）
 */
public interface SpaceUsageReportService {

    /** 车位利用率汇总：总车位/当前占用/空置率/高峰占用率/高峰时段 */
    SpaceUsageReportVO getStats(Long parkingId);

    /** 今日分小时在场车辆：{ hour(0-23), inLot }（已补齐 0-23） */
    List<Map<String, Object>> getHourly(Long parkingId);

    /** 区域占用对比：{ area, total, occupied } */
    List<Map<String, Object>> getArea(Long parkingId);
}
