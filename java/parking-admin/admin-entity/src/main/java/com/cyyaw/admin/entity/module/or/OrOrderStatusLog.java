package com.cyyaw.admin.entity.module.or;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Schema(description = "订单状态日志")
@Table(name = "or_order_status_log")
@EqualsAndHashCode(callSuper = true)
public class OrOrderStatusLog extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "order_id", columnDefinition = "bigint COMMENT '订单ID'")
    private Long orderId;

    @Column(name = "order_status", columnDefinition = "int COMMENT '订单状态'")
    private Integer orderStatus;

    @Column(name = "before_status", columnDefinition = "int COMMENT '变更前状态'")
    private Integer beforeStatus;

    @Column(name = "remark", columnDefinition = "varchar(500) COMMENT '变更说明'")
    private String remark;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "operator_id", columnDefinition = "bigint COMMENT '操作人ID'")
    private Long operatorId;

    @Column(name = "operator_type", columnDefinition = "int COMMENT '操作人类型{1:系统,2:用户,3:管理员}'")
    private Integer operatorType;

}