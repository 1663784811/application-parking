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
@Schema(description = "设备-分类")
@Table(name = "iot_classification")
@EqualsAndHashCode(callSuper = true)
public class IotClassification extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "pid", columnDefinition = "bigint COMMENT '父分组ID'")
    private Long pid;


    @Basic
    @Column(name = "name", columnDefinition = "varchar(255) default '' COMMENT '分类名称'")
    private String name;

    @Column(name = "icon", columnDefinition = "varchar(100) COMMENT '图标'")
    private String icon;


}
