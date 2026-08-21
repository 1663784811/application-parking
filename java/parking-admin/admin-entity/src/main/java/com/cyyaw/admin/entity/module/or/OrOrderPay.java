package com.cyyaw.admin.entity.module.or;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Schema(description = "订单支付记录")
@Table(name = "or_order_pay")
@EqualsAndHashCode(callSuper = true)
public class OrOrderPay extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "order_id", columnDefinition = "bigint COMMENT '订单ID'")
    private Long orderId;

    @Column(name = "pay_no", columnDefinition = "varchar(50) COMMENT '支付流水号'")
    private String payNo;

    @Column(name = "pay_type", columnDefinition = "int COMMENT '支付方式{1:支付宝,2:微信支付,3:银行卡,4:现金}'")
    private Integer payType;

    @Column(name = "pay_amount", columnDefinition = "decimal(18,2) COMMENT '支付金额'")
    private BigDecimal payAmount;

    @Column(name = "pay_status", columnDefinition = "int default 0 COMMENT '支付状态{0:未支付,1:支付中,2:支付成功,3:支付失败}'")
    private Integer payStatus;

    @Column(name = "pay_time", columnDefinition = "datetime COMMENT '支付时间'")
    private LocalDateTime payTime;

    @Column(name = "callback_time", columnDefinition = "datetime COMMENT '回调时间'")
    private LocalDateTime callbackTime;

    @Column(name = "callback_content", columnDefinition = "text COMMENT '回调内容'")
    private String callbackContent;

    @Column(name = "refund_no", columnDefinition = "varchar(50) COMMENT '退款流水号'")
    private String refundNo;

    @Column(name = "refund_amount", columnDefinition = "decimal(18,2) COMMENT '退款金额'")
    private BigDecimal refundAmount;

    @Column(name = "refund_time", columnDefinition = "datetime COMMENT '退款时间'")
    private LocalDateTime refundTime;

}