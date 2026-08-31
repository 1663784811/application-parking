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
@Schema(description = "设备命令")
@Table(name = "iot_device_command")
@EqualsAndHashCode(callSuper = true)
public class IotDeviceCommand extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "device_id", columnDefinition = "bigint COMMENT '设备ID'")
    private Long deviceId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "sender_id", columnDefinition = "bigint COMMENT '发送人ID'")
    private Long senderId;
    //=====================================
    @Column(name = "command_name", columnDefinition = "varchar(100) COMMENT '指令名称'")
    private String commandName;

    @Column(name = "command_code", columnDefinition = "varchar(50) COMMENT '指令编码'")
    private String commandCode;

    @Column(name = "command_content", columnDefinition = "text COMMENT '指令内容'")
    private String commandContent;

    @Column(name = "command_status", columnDefinition = "int default 0 COMMENT '指令状态{0:待执行,1:执行中,2:执行成功,3:执行失败}'")
    private Integer commandStatus;

    @Column(name = "response_content", columnDefinition = "text COMMENT '响应内容'")
    private String responseContent;

    @Column(name = "timeout", columnDefinition = "int COMMENT '超时时间(秒)'")
    private Integer timeout;

    @Column(name = "sender_type", columnDefinition = "int COMMENT '发送人类型{1:系统,2:用户,3:管理员}'")
    private Integer senderType;

    @Column(name = "send_time", columnDefinition = "datetime COMMENT '发送时间'")
    private LocalDateTime sendTime;

    @Column(name = "execute_time", columnDefinition = "datetime COMMENT '执行时间'")
    private LocalDateTime executeTime;
} 