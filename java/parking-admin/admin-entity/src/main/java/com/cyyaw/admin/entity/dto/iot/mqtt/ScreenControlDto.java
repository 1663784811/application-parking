package com.cyyaw.admin.entity.dto.iot.mqtt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "显示屏控制参数")
public class ScreenControlDto {

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "显示内容")
    private String msg;

}
