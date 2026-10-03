package com.cyyaw.admin.application.parking;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 停车费计费明细：一条 {@code PkCostRules} 在一个计费区间内的收费记录。
 * <p>
 * 由 {@link ParkingCost#getParkingCostDetails()} 承载，用于向车主展示"这笔钱是怎么算出来的"：
 * 首段阶梯的每一档、计费时段逐段的收费各产生一条明细。
 * <p>
 * {@link #startTime}/{@link #endTime} 描述本条明细覆盖的实际停车区间（不是规则的
 * 配置时段），便于逐段核对；同一停车记录的各明细区间首尾相接，合计等于本次停车时长。
 * <p>
 * 金额单位与 {@code pk_cost_rules.amount} 一致（元，两位小数）；封顶规则不产生明细，
 * 只体现在 {@link ParkingCost} 的应付总额上。
 */
@Data
public class ParkingCostDetails {

    /**
     * 收费规则ID（{@code pk_cost_rules.id}）
     */
    private Long ruleId;

    /**
     * 收费规则名称（{@code pk_cost_rules.name}，冗余存储便于展示，免二次查询）
     */
    private String ruleName;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 收取费用（本区间实际收费金额）
     */
    private BigDecimal amount;

}
