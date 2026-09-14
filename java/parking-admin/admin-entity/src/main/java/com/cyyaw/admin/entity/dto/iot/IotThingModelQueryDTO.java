package com.cyyaw.admin.entity.dto.iot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "物模型列表查询参数")
public class IotThingModelQueryDTO extends PageDTO {

    @Schema(description = "物模型名称模糊关键词")
    private String keyword;
}
