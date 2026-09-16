package com.cyyaw.admin.entity.dto.parking;

import lombok.Data;

/**
 * 车位利用率报表汇总 VO
 */
@Data
public class SpaceUsageReportVO {
    /** 总车位数 */
    private Integer totalSpaces;
    /** 当前在场车辆（占用） */
    private Integer currentIn;
    /** 空置率（百分比，0-100） */
    private Integer vacancyRate;
    /** 高峰占用率（百分比） */
    private Integer peakRate;
    /** 高峰时段，如 "09:00-10:00"；当日无在场记 "无" */
    private String peakHour;
}
