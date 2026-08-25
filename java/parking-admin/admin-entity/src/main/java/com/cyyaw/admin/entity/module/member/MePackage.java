package com.cyyaw.admin.entity.module.member;

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
@Schema(description = "会员套餐")
@Table(name = "me_package")
@EqualsAndHashCode(callSuper = true)
public class MePackage extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @Column(name = "name", columnDefinition = "varchar(255) COMMENT '套餐名称'")
    private String name;

    @Column(name = "type", columnDefinition = "int COMMENT '套餐类型{1:月卡,2:季卡,3:年卡}'")
    private Integer type;

    @Column(name = "valid_days", columnDefinition = "int COMMENT '有效期天数'")
    private Integer validDays;

    @Column(name = "price", columnDefinition = "decimal(18,2) COMMENT '售价'")
    private BigDecimal price;

    @Column(name = "parking_ids", columnDefinition = "varchar(255) COMMENT '适用停车场ID(逗号分隔)'")
    private String parkingIds;

    @Column(name = "sales", columnDefinition = "int default 0 COMMENT '已售数量'")
    private Integer sales;

}
