package com.cyyaw.admin.entity.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * H5 订单列表 / 订单详情项。
 * <p>
 * 字段名对齐前端 Order.vue / OrderDetail.vue 既有渲染结构，页面只需去掉占位数据。
 * 一条订单展示一行停车明细（停车订单的 or_order_detail.business_id 就是 pk_car_log.id），
 * 所以车牌、入场出场时间、停车场名称都随订单一起带出来，前端不用再 join。
 * <p>
 * status 语义与前端一致：0 待支付、1 已支付、2 已完成。
 * 由 pay_status 推导 —— 已支付且订单已完成（order_status=4）才算完成。
 */
@Data
@Schema(description = "H5 订单")
public class AppOrderVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "0 待支付，1 已支付，2 已完成")
    private Integer status;

    @Schema(description = "状态文案")
    private String statusText;

    @Schema(description = "车牌号")
    private String plateNumber;

    @Schema(description = "停车场名称")
    private String parkingName;

    @Schema(description = "停车场地址")
    private String parkingAddress;

    @Schema(description = "入场时间（yyyy-MM-dd HH:mm:ss）")
    private String entryTime;

    @Schema(description = "出场时间（yyyy-MM-dd HH:mm:ss），未出场时为 null")
    private String exitTime;

    @Schema(description = "停车时长文案，如「2小时30分钟」；未出场时为 null")
    private String duration;

    @Schema(description = "订单总金额")
    private BigDecimal amount;

    @Schema(description = "优惠金额")
    private BigDecimal discount;

    @Schema(description = "实付金额")
    private BigDecimal payAmount;

}
