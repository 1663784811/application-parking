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

@Data
@Entity
@Schema(description = "订单详情")
@Table(name = "or_order_detail")
@EqualsAndHashCode(callSuper = true)
public class OrOrderDetail extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "order_id", columnDefinition = "bigint COMMENT '订单ID'")
    private Long orderId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "business_id", columnDefinition = "bigint COMMENT '所属业务ID,比如停车场ID、商品ID'")
    private Long businessId;

    // ===============================================================================

    @Column(name = "product_name", columnDefinition = "varchar(100) COMMENT '商品名称'")
    private String productName;

    @Column(name = "product_image", columnDefinition = "varchar(255) COMMENT '商品图片'")
    private String productImage;

    @Column(name = "product_price", columnDefinition = "decimal(18,2) COMMENT '商品单价'")
    private BigDecimal productPrice;

    @Column(name = "quantity", columnDefinition = "int COMMENT '购买数量'")
    private Integer quantity;

    @Column(name = "total_amount", columnDefinition = "decimal(18,2) COMMENT '订单总金额'")
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", columnDefinition = "decimal(18,2) COMMENT '优惠金额'")
    private BigDecimal discountAmount;

}