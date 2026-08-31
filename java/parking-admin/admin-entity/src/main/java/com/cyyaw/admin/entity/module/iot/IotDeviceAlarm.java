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
@Schema(description = "设备告警信息")
@Table(name = "iot_device_alarm")
@EqualsAndHashCode(callSuper = true)
public class IotDeviceAlarm extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "device_id", columnDefinition = "bigint COMMENT '设备ID'")
    private Long deviceId;

    // ======================

    @Column(name = "alarm_name", columnDefinition = "varchar(100) COMMENT '告警名称'")
    private String alarmName;

    @Column(name = "alarm_code", columnDefinition = "varchar(50) COMMENT '告警编码'")
    private String alarmCode;

    @Column(name = "alarm_level", columnDefinition = "int COMMENT '告警级别{1:提示,2:一般,3:重要,4:紧急}'")
    private Integer alarmLevel;

    @Column(name = "alarm_type", columnDefinition = "varchar(50) COMMENT '告警类型'")
    private String alarmType;

    @Column(name = "alarm_content", columnDefinition = "text COMMENT '告警内容'")
    private String alarmContent;

    @Column(name = "alarm_time", columnDefinition = "datetime COMMENT '告警时间'")
    private LocalDateTime alarmTime;

    @Column(name = "handle_status", columnDefinition = "int default 0 COMMENT '处理状态{0:未处理,1:处理中,2:已处理,3:已忽略}'")
    private Integer handleStatus;

    @Column(name = "handler_id", columnDefinition = "bigint COMMENT '处理人ID'")
    private Long handlerId;

    @Column(name = "handle_time", columnDefinition = "datetime COMMENT '处理时间'")
    private LocalDateTime handleTime;

    @Column(name = "handle_result", columnDefinition = "text COMMENT '处理结果'")
    private String handleResult;
} 