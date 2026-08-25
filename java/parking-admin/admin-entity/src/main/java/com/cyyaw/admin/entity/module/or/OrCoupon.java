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
@Schema(description = "优惠券")
@Table(name = "or_coupon")
@EqualsAndHashCode(callSuper = true)
public class OrCoupon extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @Column(name = "name", columnDefinition = "varchar(255) COMMENT '优惠券名称'")
    private String name;

    @Column(name = "type", columnDefinition = "int COMMENT '优惠券类型{1:满减券,2:折扣券,3:免费时长券}'")
    private Integer type;

    @Column(name = "threshold", columnDefinition = "decimal(18,2) COMMENT '使用门槛金额(0表示无门槛)'")
    private BigDecimal threshold;

    @Column(name = "discount", columnDefinition = "decimal(18,2) COMMENT '优惠值:满减金额/折扣率/免费时长(分钟)'")
    private BigDecimal discount;

    @Column(name = "total_count", columnDefinition = "int COMMENT '发放数量'")
    private Integer totalCount;

    @Column(name = "used_count", columnDefinition = "int default 0 COMMENT '已使用数量'")
    private Integer usedCount;

    @Column(name = "valid_days", columnDefinition = "int COMMENT '领取后有效天数'")
    private Integer validDays;

    @Column(name = "status", columnDefinition = "int default 1 COMMENT '状态{0:停用,1:启用}'")
    private Integer status;

}
