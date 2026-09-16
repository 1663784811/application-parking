package com.cyyaw.admin.entity.dto.parking;

import lombok.Data;

/**
 * 车流量报表汇总 VO
 */
@Data
public class TrafficReportVO {
    /** 今日入场车次 */
    private Integer todayIn;
    /** 今日出场车次 */
    private Integer todayOut;
    /** 当前在场车辆 */
    private Integer currentIn;
    /** 今日入场峰值时段，如 "09:00-10:00"；当日无入场记 "无" */
    private String peakHour;
}
