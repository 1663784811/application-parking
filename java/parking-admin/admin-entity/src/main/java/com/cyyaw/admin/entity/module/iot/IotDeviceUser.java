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
@Schema(description = "设备_用户表")
@Table(name = "iot_device_user")
@EqualsAndHashCode(callSuper = true)
public class IotDeviceUser extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "user_id", columnDefinition = "bigint COMMENT '用户ID'")
    private Long userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "device_group_id", columnDefinition = "bigint COMMENT '分组ID'")
    private Long deviceGroupId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "device_id", columnDefinition = "bigint COMMENT '设备ID'")
    private Long deviceId;

    @Basic
    @Column(name = "role", columnDefinition = "varchar(255) default '' COMMENT '角色'")
    private String role;

    @Basic
    @Column(name = "name", columnDefinition = "varchar(255) default '' COMMENT '名称'")
    private String name;

} 