package com.cyyaw.admin.entity.module.parking;

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
@Schema(description = "停车记录")
@Table(name = "pk_car_log")
@EqualsAndHashCode(callSuper = true)
public class PkCarLog extends BaseEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "app_id", columnDefinition = "bigint COMMENT '应用ID'")
    private Long appId;

    @Column(name = "parking_id", columnDefinition = "bigint COMMENT '停车场ID'")
    private Long parkingId;

    // ===================================================================

    @Column(name = "car_number", columnDefinition = "varchar(20) COMMENT '车牌号码'")
    private String carNumber;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "entry_time", columnDefinition = "datetime COMMENT '入场时间'")
    private LocalDateTime entryTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "out_time", columnDefinition = "datetime COMMENT '出场时间'")
    private LocalDateTime outTime;

    @Column(name = "status", columnDefinition = "int COMMENT '状态{0:场内,1:已出场}'")
    private Integer status;

    @Column(name = "car_type", columnDefinition = "varchar(20) COMMENT '车辆类型'")
    private String carType;

    // ===================================================================
    // 出场通道信息：出口摄像头识别到车牌时写入，标识「该车正在某通道等待缴费出场」。
    // 待缴费期间 status 仍为 0（场内），缴费放行后才置 1 并写 outTime。

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(name = "out_channel_id", columnDefinition = "bigint COMMENT '出场通道ID'")
    private Long outChannelId;

    @Column(name = "out_device_code", columnDefinition = "varchar(64) COMMENT '出场识别设备编码'")
    private String outDeviceCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "out_recognize_time", columnDefinition = "datetime COMMENT '出场识别时间'")
    private LocalDateTime outRecognizeTime;

}