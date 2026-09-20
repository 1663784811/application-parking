package com.cyyaw.admin.entity.dto.parking;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口1响应：某出场通道当前正在等待缴费出场的车辆。
 * 出口摄像头识别到车牌后写入 pk_car_log 的出场通道字段，H5 页面加载时据此预填车牌。
 */
@Data
@Schema(description = "通道当前待出场车辆")
public class ExitChannelVehicleVO {

    @Schema(description = "车牌号码")
    private String carNumber;

    @Schema(description = "车辆类型")
    private String carType;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "停车场ID")
    private Long parkingId;

    @Schema(description = "停车场名称")
    private String parkingName;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "出场通道ID")
    private Long channelId;

    @Schema(description = "出场通道名称")
    private String channelName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "入场时间")
    private LocalDateTime entryTime;

}
