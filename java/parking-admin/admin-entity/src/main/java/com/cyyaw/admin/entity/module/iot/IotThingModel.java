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
@Schema(description = "物模型")
@Table(name = "iot_thing_model")
@EqualsAndHashCode(callSuper = true)
public class IotThingModel extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @Basic
    @Column(name = "name", columnDefinition = "varchar(255) default '' COMMENT '名称'")
    private String name;

    @Basic
    @Column(name = "data", columnDefinition = "text COMMENT '数据'")
    private String data;

}
