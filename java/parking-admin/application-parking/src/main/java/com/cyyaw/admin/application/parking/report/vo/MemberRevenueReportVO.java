package com.cyyaw.admin.application.parking.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 月卡营收汇总 VO
 */
@Data
@Schema(description = "月卡营收汇总")
public class MemberRevenueReportVO {
    /** 续费总金额 */
    @Schema(description = "续费总金额")
    private BigDecimal totalRevenue;
    /** 续费笔数 */
    @Schema(description = "续费笔数")
    private Integer renewalCount;
    /** 活跃会员数（member_id 去重） */
    @Schema(description = "活跃会员数（去重）")
    private Integer memberCount;
    /** 人均续费金额 */
    @Schema(description = "人均续费金额")
    private BigDecimal avgRevenue;
    /** 峰值月份（yyyy-MM），无数据记 "无" */
    @Schema(description = "峰值月份（yyyy-MM）")
    private String peakMonth;
    /** 峰值月份营收金额 */
    @Schema(description = "峰值月份营收金额")
    private BigDecimal peakRevenue;
}
