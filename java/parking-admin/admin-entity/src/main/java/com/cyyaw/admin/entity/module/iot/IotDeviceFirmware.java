package com.cyyaw.admin.entity.module.iot;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Entity
@Schema(description = "设备固件")
@Table(name = "iot_device_firmware")
@EqualsAndHashCode(callSuper = true)
public class IotDeviceFirmware extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @Column(name = "name", columnDefinition = "varchar(100) COMMENT '固件名称'")
    private String name;

    @Column(name = "firmware_version", columnDefinition = "varchar(50) COMMENT '固件版本'")
    private String firmwareVersion;

    @Column(name = "device_type", columnDefinition = "varchar(50) COMMENT '设备类型'")
    private String deviceType;

    @Column(name = "file_url", columnDefinition = "varchar(500) COMMENT '固件文件URL'")
    private String fileUrl;

    @Column(name = "file_size", columnDefinition = "bigint COMMENT '文件大小(字节)'")
    private Long fileSize;

    @Column(name = "md5", columnDefinition = "varchar(32) COMMENT '文件MD5值'")
    private String md5;

    @Column(name = "description", columnDefinition = "varchar(500) COMMENT '固件描述'")
    private String description;

    @Column(name = "status", columnDefinition = "int default 1 COMMENT '状态{0:禁用,1:启用}'")
    private Integer status;
} 