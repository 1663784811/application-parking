package com.cyyaw.admin.application.parking.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 工作台核心指标 VO（跨 or_order / pk_car_log / pk_parking 聚合）。
 */
@Data
public class DashboardStatsVO {

    /** 今日总营收 */
    private BigDecimal todayRevenue;

    /** 昨日营收（用于计算同比） */
    private BigDecimal yesterdayRevenue;

    /** 营收同比（今日较昨日，单位 %，昨日为 0 时记 0 避免除零） */
    private BigDecimal revenueTrend;

    /** 当前在场车辆 */
    private Integer currentVehicles;

    /** 总车位（SUM(pk_parking.capacity)） */
    private Integer totalSpaces;

    /** 车位利用率（%，= currentVehicles / totalSpaces * 100） */
    private Integer spaceUsageRate;

    /** 今日进场车次 */
    private Integer todayIn;

    /** 今日出场车次 */
    private Integer todayOut;

    /** 待处理异常订单（= 未支付订单数） */
    private Integer exceptionCount;

    /** 欠费订单数 */
    private Integer unpaidCount;

    /** 无牌在场车辆数 */
    private Integer noPlateCount;

    /** 设备故障数（占位 0，待设备故障统计对接） */
    private Integer faultCount;
}
