package com.cyyaw.admin.entity.module.or;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Schema(description = "订单")
@Table(name = "or_order")
@EqualsAndHashCode(callSuper = true)
public class OrOrder extends BaseEntity {

    @Column(name = "order_no", columnDefinition = "varchar(50) COMMENT '订单编号'")
    private String orderNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "store_id", columnDefinition = "bigint COMMENT '所属门店ID'")
    private Long storeId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "user_id", columnDefinition = "bigint COMMENT '用户ID'")
    private Long userId;

    // ==============================================================================

    @Column(name = "total_amount", columnDefinition = "decimal(18,2) COMMENT '总订单总金额 = 所有订单详情总计'")
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", columnDefinition = "decimal(18,2) COMMENT '总优惠金额 = 所有订单详情优惠 + 订单优惠'")
    private BigDecimal discountAmount;

    @Column(name = "pay_amount", columnDefinition = "decimal(18,2) COMMENT '实付金额 = 总订单总金额-总优惠金额'")
    private BigDecimal payAmount;

    @Column(name = "pay_amounted", columnDefinition = "decimal(18,2) COMMENT '已付金额'")
    private BigDecimal payAmounted;

    @Column(name = "pay_status", columnDefinition = "int default 0 COMMENT '支付状态{0:未支付,1:支付部分,2:已支付,3:部分退款,4:全部退款}'")
    private Integer payStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "pay_time", columnDefinition = "datetime COMMENT '最后支付时间'")
    private LocalDateTime payTime;

    @Column(name = "delivery_status", columnDefinition = "int default 0 COMMENT '发货状态{1:待发货,2:已发货,3:已签收,4:退货中,5:退货签收}'")
    private Integer deliveryStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "delivery_time", columnDefinition = "datetime COMMENT '发货时间'")
    private LocalDateTime deliveryTime;

    @Column(name = "order_status", columnDefinition = "int default 0 COMMENT '订单状态{0:待付款,2:待发货,3:待收货,4:已完成,5:申请售后,6:取消中,7:已取消}'")
    private Integer orderStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "complete_time", columnDefinition = "datetime COMMENT '完成时间'")
    private LocalDateTime completeTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "cancel_time", columnDefinition = "datetime COMMENT '取消时间'")
    private LocalDateTime cancelTime;

    @Column(name = "remark", columnDefinition = "varchar(500) COMMENT '订单备注'")
    private String remark;

    // ========================
    @Transient
    private List<OrOrderDetail> orderDetailList;

}