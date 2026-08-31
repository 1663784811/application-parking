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
@Schema(description = "设备")
@Table(name = "iot_device")
@EqualsAndHashCode(callSuper = true)
public class IotDevice extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "business_id", columnDefinition = "bigint COMMENT '所属业务ID,比如停车场ID'")
    private Long businessId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "classification_id", columnDefinition = "bigint COMMENT '分类表ID'")
    private Long classificationId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "thing_model_id", columnDefinition = "bigint COMMENT '物模型ID'")
    private Long thingModelId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "product_id", columnDefinition = "bigint COMMENT '产品ID'")
    private Long productId;

    // =====================================================================

    @Column(name = "name", columnDefinition = "varchar(100) COMMENT '设备名称'")
    private String name;

    @Column(name = "code", columnDefinition = "varchar(50) unique COMMENT '设备编码'")
    private String code;

    @Column(name = "type", columnDefinition = "varchar(50) COMMENT '设备类型{switch:开关,light:电灯,aircondition:空调,refrigerator:冰箱}'")
    private String type;

    @Column(name = "model", columnDefinition = "varchar(50) COMMENT '设备型号'")
    private String model;

    @Column(name = "serial_no", columnDefinition = "varchar(50) COMMENT '设备序列号'")
    private String serialNo;

    @Column(name = "mac_address", columnDefinition = "varchar(50) COMMENT 'MAC地址'")
    private String macAddress;

    @Column(name = "ip_address", columnDefinition = "varchar(50) COMMENT 'IP地址'")
    private String ipAddress;

    @Column(name = "firmware_version", columnDefinition = "varchar(50) COMMENT '固件版本'")
    private String firmwareVersion;

    @Column(name = "device_type", columnDefinition = "varchar(50) COMMENT '设备类型{connect:直连,gateway:网关,subDevice:子设备}'")
    private String deviceType;

    @Column(name = "connect_type", columnDefinition = "varchar(50) COMMENT '连接方式{1:WiFi,2:蓝牙,3:有线,4:4G/5G,5:其他}'")
    private String connectType;

    @Column(name = "online_status", columnDefinition = "int default 0 COMMENT '在线状态{0:离线,1:在线}'")
    private Integer onlineStatus;

    @Column(name = "work_status", columnDefinition = "int default 0 COMMENT '工作状态{0:停用,1:正常,2:故障,3:维护中}'")
    private Integer workStatus;

    @Column(name = "location", columnDefinition = "varchar(255) COMMENT '安装位置'")
    private String location;

    @Column(name = "location_type", columnDefinition = "int COMMENT '位置类型{1:停车场入口,2:停车场出口}'")
    private Integer locationType;

    @Column(name = "long_lat", columnDefinition = "varchar(64) COMMENT '经纬度'")
    private BigDecimal longLat;

    @Column(name = "activate_time", columnDefinition = "datetime COMMENT '激活时间'")
    private LocalDateTime activateTime;

    @Column(name = "last_online_time", columnDefinition = "datetime COMMENT '最后在线时间'")
    private LocalDateTime lastOnlineTime;

    @Column(name = "description", columnDefinition = "varchar(500) COMMENT '设备描述'")
    private String description;

    @Column(name = "status", columnDefinition = "int default 1 COMMENT '状态{0:禁用,1:启用}'")
    private Integer status;

    @Column(name = "icon", columnDefinition = "varchar(20) COMMENT 'icon'")
    private String icon;

    @Column(name = "username", columnDefinition = "varchar(20) COMMENT '设备用户名'")
    private String username;

    @Column(name = "password", columnDefinition = "varchar(64) COMMENT '密码'")
    private String password;


} 