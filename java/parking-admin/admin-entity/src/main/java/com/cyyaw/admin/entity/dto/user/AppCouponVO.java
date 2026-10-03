package com.cyyaw.admin.entity.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * H5 优惠券列表项（or_coupon 的只读展示映射）。
 * <p>
 * 字段名对齐前端 Coupon.vue 既有渲染结构（id/name/description/amount/condition/expireTime/status），
 * 页面只需去掉占位数据，不用改模板。
 * <p>
 * status 语义与前端一致：0 可用、1 已使用、2 已过期。
 * or_coupon 表本身只有 启用/停用（0/1）一个状态位，
 * 「已使用」判定为 已使用数量达到发放数量，「已过期」判定为 领取基数 + 有效天数 已早于当前时间。
 */
@Data
@Schema(description = "H5 优惠券")
public class AppCouponVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "优惠券ID")
    private Long id;

    @Schema(description = "优惠券名称")
    private String name;

    @Schema(description = "说明：按类型拼出的使用规则文案")
    private String description;

    @Schema(description = "优惠值：满减为金额，折扣为折扣率（0.7 表示 7 折）")
    private BigDecimal amount;

    @Schema(description = "使用门槛文案，如「无门槛」「满30可用」")
    private String condition;

    @Schema(description = "有效期到期文案（领取后 N 天折算），无有效天数时为 null")
    private String expireTime;

    @Schema(description = "0 可用，1 已使用，2 已过期")
    private Integer status;

    @Schema(description = "发放数量")
    private Integer totalCount;

    @Schema(description = "已使用数量")
    private Integer usedCount;

}
