package com.cyyaw.admin.entity.dto.parking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "未绑定通道设备查询参数")
public class PkParkingDeviceUnboundDTO {

    @Schema(description = "停车场ID（为空时查全部未绑设备）")
    private Long parkingId;
}
