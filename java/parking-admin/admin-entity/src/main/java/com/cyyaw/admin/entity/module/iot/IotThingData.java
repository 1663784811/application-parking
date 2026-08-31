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
@Schema(description = "物模型-数据")
@Table(name = "iot_thing_data")
@EqualsAndHashCode(callSuper = true)
public class IotThingData extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "device_id", columnDefinition = "bigint NOT NULL COMMENT '设备ID'")
    private Long deviceId;

    @Basic
    @Column(name = "attribute_data", columnDefinition = "text COMMENT '属性数据'")
    private String attributeData;
}
