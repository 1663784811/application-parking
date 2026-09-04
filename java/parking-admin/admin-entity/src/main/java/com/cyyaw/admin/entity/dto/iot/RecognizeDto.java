package com.cyyaw.admin.entity.dto.iot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "车牌识别")
public class RecognizeDto {

    @Schema(description = "识别设备编码")
    private String deviceCode;
    @Schema(description = "车牌号")
    private String carNumber;
    @Schema(description = "车辆类型")
    private String carType;

}
