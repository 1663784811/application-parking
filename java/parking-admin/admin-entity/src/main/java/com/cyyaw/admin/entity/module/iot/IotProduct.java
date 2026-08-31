package com.cyyaw.admin.entity.module.iot;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Schema(description = "产品")
@Table(name = "iot_product")
@EqualsAndHashCode(callSuper = true)
public class IotProduct extends BaseEntity {


    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "thing_model_id", columnDefinition = "bigint COMMENT '物模型ID'")
    private Long thingModelId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "factory_id", columnDefinition = "bigint COMMENT '厂商ID'")
    private Long factoryId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "classification_id", columnDefinition = "bigint COMMENT '分类表ID'")
    private Long classificationId;

    // =====================================================================

    @Basic
    @Column(name = "name", columnDefinition = "varchar(255) default '' COMMENT '名称'")
    private String name;

    @Basic
    @Column(name = "batch", columnDefinition = "varchar(255) COMMENT '批次'")
    private String batch;

    @Basic
    @Column(name = "img", columnDefinition = "text COMMENT '图片'")
    private String img;

}
