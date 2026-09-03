package com.cyyaw.application.parking.gate.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 车牌识别回调请求体。
 * <p>字段对齐 admin 侧 com.cyyaw.netty.mqtt.ctl.enity.CarInfo（不同模块不可直接引用，
 * 故本地重建同名字段），并增加通行记录所需的 gate/type。</p>
 */
@Data
public class PlateRecognitionRequest {

    @Schema(description = "硬件编号（摄像头/设备）", example = "CAM001")
    private String deviceCode;

    @Schema(description = "车牌号（必填）", example = "粤B·8K321")
    private String carNumber;

    @Schema(description = "车辆类型", example = "小型车")
    private String carType;

    @Schema(description = "车辆图片base64 ")
    private String img;

    @Schema(description = "车牌图片base64 ")
    private String numberImg;
}
