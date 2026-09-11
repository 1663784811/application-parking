package com.cyyaw.admin.entity.dto.iot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "设备上线/下线")
public class OnlineDto {

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "在线状态：true=上线, false=离线")
    private Boolean online;

}
