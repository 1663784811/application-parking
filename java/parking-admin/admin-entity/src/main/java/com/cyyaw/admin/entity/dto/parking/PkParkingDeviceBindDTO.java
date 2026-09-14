package com.cyyaw.admin.entity.dto.parking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "设备绑定通道参数")
public class PkParkingDeviceBindDTO {

    @Schema(description = "通道类型(in/out)，出入口摄像头需传")
    private String channelType;
}
