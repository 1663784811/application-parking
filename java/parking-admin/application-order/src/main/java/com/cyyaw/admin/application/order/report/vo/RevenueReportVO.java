package com.cyyaw.admin.application.order.report.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 营收报表汇总
 */
@Data
public class RevenueReportVO {

    /** 营收总额（线上+线下） */
    private BigDecimal totalRevenue;

    /** 线上营收（支付宝/微信） */
    private BigDecimal onlineRevenue;

    /** 线下营收（银行卡/现金） */
    private BigDecimal offlineRevenue;

    /** 线上占比 % */
    private Integer onlinePercent;

    /** 线下占比 % */
    private Integer offlinePercent;

    /** 优惠抵扣合计 */
    private BigDecimal discount;

    /** 已支付订单数 */
    private Integer orderCount;

    /** 营收同比 %（对比上一等长周期，上一周期为 0 时记 0） */
    private BigDecimal revenueTrend;
}
