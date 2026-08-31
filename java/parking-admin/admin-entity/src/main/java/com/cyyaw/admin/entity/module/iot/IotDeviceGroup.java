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
@Schema(description = "设备-分组")
@Table(name = "iot_device_group")
@EqualsAndHashCode(callSuper = true)
public class IotDeviceGroup extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "user_id", columnDefinition = "bigint COMMENT '用户ID'")
    private Long userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "pid", columnDefinition = "bigint COMMENT '父分组ID'")
    private Long pid;



    @Column(name = "name", columnDefinition = "varchar(100) COMMENT '分组名称'")
    private String name;

    @Column(name = "icon", columnDefinition = "varchar(100) COMMENT '图标'")
    private String icon;

    @Column(name = "description", columnDefinition = "varchar(255) COMMENT '分组描述'")
    private String description;

} 