package com.cyyaw.admin.entity.dto.iot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Data
@Schema(description = "物模型子实体计数查询参数")
public class IotThingModelSubDTO {

    @Schema(description = "物模型ID集合")
    private List<Long> modelIds;
}
