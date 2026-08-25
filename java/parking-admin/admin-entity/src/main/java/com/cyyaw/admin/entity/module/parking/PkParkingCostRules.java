package com.cyyaw.admin.entity.module.parking;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 停车场收费规则关联（多对多中间表）。
 * <p>一条收费规则可适用多个停车场，一个停车场可有多条收费规则。
 */
@Data
@Entity
@Schema(description = "停车场收费规则关联")
@Table(name = "pk_parking_cost_rules")
@EqualsAndHashCode(callSuper = true)
public class PkParkingCostRules extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "parking_id", columnDefinition = "bigint COMMENT '停车场ID'")
    private Long parkingId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "cost_rules_id", columnDefinition = "bigint COMMENT '收费规则ID'")
    private Long costRulesId;

}
