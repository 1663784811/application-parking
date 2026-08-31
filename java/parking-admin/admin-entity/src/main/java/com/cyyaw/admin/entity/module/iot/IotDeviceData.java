package com.cyyaw.admin.entity.module.iot;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Schema(description = "设备数据")
@Table(name = "iot_device_data")
@EqualsAndHashCode(callSuper = true)
public class IotDeviceData extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "device_id", columnDefinition = "bigint COMMENT '设备ID'")
    private Long deviceId;

    @Column(name = "data_type", columnDefinition = "varchar(50) COMMENT '数据类型'")
    private String dataType;

    @Column(name = "data_value", columnDefinition = "decimal(18,6) COMMENT '数据值'")
    private BigDecimal dataValue;

    @Column(name = "unit", columnDefinition = "varchar(20) COMMENT '单位'")
    private String unit;

    @Column(name = "collect_time", columnDefinition = "datetime COMMENT '采集时间'")
    private LocalDateTime collectTime;
} 