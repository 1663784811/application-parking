package com.cyyaw.admin.entity.module.iot;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Schema(description = "设备故障工单")
@Table(name = "iot_device_fault")
@EqualsAndHashCode(callSuper = true)
public class IotDeviceFault extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @Column(name = "order_no", columnDefinition = "varchar(64) COMMENT '工单号'")
    private String orderNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "device_id", columnDefinition = "bigint COMMENT '设备ID'")
    private Long deviceId;

    @Column(name = "device_name", columnDefinition = "varchar(128) COMMENT '设备名称(快照)'")
    private String deviceName;

    @Column(name = "device_type", columnDefinition = "varchar(32) COMMENT '设备类型(快照)'")
    private String deviceType;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "parking_id", columnDefinition = "bigint COMMENT '停车场ID(快照)'")
    private Long parkingId;

    @Column(name = "parking_name", columnDefinition = "varchar(128) COMMENT '停车场名称(快照)'")
    private String parkingName;

    @Column(name = "fault_type", columnDefinition = "varchar(128) COMMENT '故障类型'")
    private String faultType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "report_time", columnDefinition = "datetime COMMENT '上报时间'")
    private LocalDateTime reportTime;

    @Column(name = "repairer", columnDefinition = "varchar(64) COMMENT '处理人'")
    private String repairer;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "expect_complete_time", columnDefinition = "datetime COMMENT '预计完成时间'")
    private LocalDateTime expectCompleteTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "complete_time", columnDefinition = "datetime COMMENT '实际完成时间'")
    private LocalDateTime completeTime;

    @Column(name = "status", columnDefinition = "varchar(32) default 'pending' COMMENT '处理状态{pending:待处理,processing:处理中,completed:已完成}'")
    private String status;

}
