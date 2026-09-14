package com.cyyaw.admin.entity.dto.iot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "设备故障工单列表查询参数")
public class IotDeviceFaultQueryDTO extends PageDTO {

    @Schema(description = "处理状态")
    private String status;

    @Schema(description = "上报起始日期(yyyy-MM-dd)")
    private String startTime;

    @Schema(description = "上报结束日期(yyyy-MM-dd)")
    private String endTime;
}
