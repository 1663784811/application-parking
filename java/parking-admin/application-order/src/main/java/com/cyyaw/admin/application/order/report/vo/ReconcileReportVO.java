package com.cyyaw.admin.application.order.report.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单对账汇总
 */
@Data
public class ReconcileReportVO {

    /** 营收总额（线上+线下） */
    private BigDecimal totalRevenue;

    /** 已支付订单总数 */
    private Integer totalOrders;

    /** 线上营收（支付宝/微信） */
    private BigDecimal onlineRevenue;

    /** 线上订单数 */
    private Integer onlineCount;

    /** 线下营收（银行卡/现金） */
    private BigDecimal offlineRevenue;

    /** 线下订单数 */
    private Integer offlineCount;

    /** 差异金额（总额 - 线上 - 线下，未归类支付方式金额；全为 1-4 类时为 0） */
    private BigDecimal difference;
}
