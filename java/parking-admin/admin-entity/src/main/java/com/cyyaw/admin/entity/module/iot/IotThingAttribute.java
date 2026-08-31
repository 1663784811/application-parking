package com.cyyaw.admin.entity.module.iot;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@Entity
@Schema(description = "物模型-属性")
@Table(name = "iot_thing_attribute")
@EqualsAndHashCode(callSuper = true)
public class IotThingAttribute extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "thing_model_id", columnDefinition = "bigint COMMENT '物模型ID'")
    private Long thingModelId;

    @Basic
    @Column(name = "name", columnDefinition = "varchar(64) default '' COMMENT '名称'")
    private String name;

    @Basic
    @Column(name = "prop_key", columnDefinition = "varchar(64) COMMENT '键(key)'")
    @TableField("prop_key")
    private String propKey;

    @Basic
    @Column(name = "unit", columnDefinition = "varchar(32) COMMENT '单位'")
    private String unit;

    @Basic
    @Column(name = "data_type", columnDefinition = "varchar(32) COMMENT '数据类型{number:数值,string:字符串,enumeration:枚举}'")
    private String dataType;

    @Basic
    @Column(name = "min_value", columnDefinition = "decimal(18,10) COMMENT '最小值'")
    private BigDecimal minValue;

    @Basic
    @Column(name = "max_value", columnDefinition = "decimal(18,10) COMMENT '最大值'")
    private BigDecimal maxValue;

    @Basic
    @Column(name = "data", columnDefinition = "varchar(255) COMMENT '枚举或字符串类型列表'")
    private String data;
}
