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
@Schema(description = "物模型-事件")
@Table(name = "iot_thing_event")
@EqualsAndHashCode(callSuper = true)
public class IotThingEvent extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "thing_model_id", columnDefinition = "bigint COMMENT '物模型ID'")
    private Long appId;

    @Basic
    @Column(name = "name", columnDefinition = "varchar(64) default '' COMMENT '事件名称'")
    private String name;

    @Basic
    @Column(name = "key", columnDefinition = "varchar(64) COMMENT '属性键(key)'")
    private String key;
    

}
