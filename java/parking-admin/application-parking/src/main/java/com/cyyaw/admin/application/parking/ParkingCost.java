package com.cyyaw.admin.application.parking;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 停车费结算结果：一次停车的全部费用构成。
 * <p>
 * 顶层是订单级汇总（应付总额 + 两种优惠），{@link #parkingCostDetails} 是按
 * {@code PkCostRules} 分摊出来的计费区间明细，一条明细对应一个计费规则在一个
 * 时段内的收费。
 * <p>
 * 口径：金额单位与 {@code pk_cost_rules.amount} 一致（元，两位小数），
 * 优惠金额为负向抵扣（正数表示减免了多少）。
 */
@Data
public class ParkingCost {

    /**
     * 订单ID（{@code or_order.id}）
     */
    private Long orderId;

    /**
     * 停车记录ID（{@code pk_car_log.id}）
     */
    private Long carLogId;

    /**
     * 停车场ID（{@code pk_parking.id}）
     */
    private Long parkingId;

    /**
     * 车牌号
     */
    private String carNumber;

    /**
     * 总费用（优惠抵扣后的应付金额）
     */
    private BigDecimal totalAmount;

    /**
     * 优惠券ID（{@code or_coupon.id}，未使用优惠券时为 null）
     */
    private Long couponId;

    /**
     * 时长优惠（按免费时长券等抵扣折算的金额）
     */
    private BigDecimal durationDiscount;

    /**
     * 费用优惠（按满减券、折扣券抵扣的金额）
     */
    private BigDecimal amountDiscount;

    /**
     * 收费详情
     */
    private List<ParkingCostDetails> parkingCostDetails;

}
