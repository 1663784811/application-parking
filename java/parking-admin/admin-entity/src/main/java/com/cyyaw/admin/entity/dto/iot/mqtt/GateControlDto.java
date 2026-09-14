package com.cyyaw.admin.entity.dto.iot.mqtt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "道闸控制参数")
public class GateControlDto {

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "开闸指令：true=开闸，false=关闸")
    private Boolean open;

}
