package com.cyyaw.admin.entity.module.parking;

import com.cyyaw.admin.entity.utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 停车设备：通道与设备的绑定关系（一台设备只归属一条通道）。
 * 设备明细（编码/名称/类型/在线状态等）来自 iot_device，本表只存绑定关系。
 */
@Data
@Entity
@Schema(description = "停车设备（通道-设备绑定）")
@Table(name = "pk_parking_device")
@EqualsAndHashCode(callSuper = true)
public class PkParkingDevice extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '应用ID'")
    private Long appId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "parking_id", columnDefinition = "bigint COMMENT '所属停车场ID'")
    private Long parkingId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "channel_id", columnDefinition = "bigint COMMENT '所属通道ID'")
    private Long channelId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "device_id", columnDefinition = "bigint COMMENT '设备ID'")
    private Long deviceId;

}
