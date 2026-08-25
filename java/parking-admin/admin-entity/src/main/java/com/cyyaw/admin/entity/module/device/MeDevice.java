package com.cyyaw.admin.entity.module.device;

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
@Schema(description = "设备")
@Table(name = "me_device")
@EqualsAndHashCode(callSuper = true)
public class MeDevice extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '所属APPID'")
    private Long appId;

    @Column(name = "code", columnDefinition = "varchar(64) COMMENT '设备编号'")
    private String code;

    @Column(name = "name", columnDefinition = "varchar(128) COMMENT '设备名称'")
    private String name;

    @Column(name = "type", columnDefinition = "varchar(32) COMMENT '设备类型{camera:摄像头,gate:道闸,screen:显示屏,sensor:地感}'")
    private String type;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "parking_id", columnDefinition = "bigint COMMENT '所属停车场ID'")
    private Long parkingId;

    @Column(name = "channel", columnDefinition = "varchar(64) COMMENT '安装通道'")
    private String channel;

    @Column(name = "ip", columnDefinition = "varchar(64) COMMENT 'IP地址'")
    private String ip;

    @Column(name = "online_status", columnDefinition = "int default 0 COMMENT '在线状态{0:离线,1:在线}'")
    private Integer onlineStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "last_online", columnDefinition = "datetime COMMENT '最后在线时间'")
    private LocalDateTime lastOnline;

}
