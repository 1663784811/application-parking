package com.cyyaw.admin.entity.dto.iot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "设备列表查询参数")
public class IotDeviceQueryDTO extends PageDTO {

    @Schema(description = "设备类型")
    private String type;

    @Schema(description = "在线状态(0离线/1在线)", example = "1")
    private Integer onlineStatus;

    @Schema(description = "设备编号或名称模糊关键词")
    private String keyword;
}
