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
@Schema(description = "设备固件升级记录")
@Table(name = "iot_device_firmware_upgrade")
@EqualsAndHashCode(callSuper = true)
public class IotDeviceFirmwareUpgrade extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "device_id", columnDefinition = "bigint COMMENT '设备ID'")
    private Long deviceId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "firmware_id", columnDefinition = "bigint COMMENT '固件ID'")
    private Long firmwareId;



    @Column(name = "old_version", columnDefinition = "varchar(50) COMMENT '升级前版本'")
    private String oldVersion;

    @Column(name = "new_version", columnDefinition = "varchar(50) COMMENT '升级后版本'")
    private String newVersion;

    @Column(name = "upgrade_status", columnDefinition = "int COMMENT '升级状态{0:待升级,1:升级中,2:升级成功,3:升级失败}'")
    private Integer upgradeStatus;

    @Column(name = "start_time", columnDefinition = "datetime COMMENT '开始时间'")
    private LocalDateTime startTime;

    @Column(name = "end_time", columnDefinition = "datetime COMMENT '结束时间'")
    private LocalDateTime endTime;

    @Column(name = "error_msg", columnDefinition = "varchar(500) COMMENT '错误信息'")
    private String errorMsg;
} 