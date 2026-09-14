package com.cyyaw.admin.entity.dto.iot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "物模型属性/事件/指令查询参数")
public class IotThingModelAttributeDTO {

    @Schema(description = "物模型ID")
    private Long thingModelId;
}
