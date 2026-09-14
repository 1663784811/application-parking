package com.cyyaw.admin.entity.dto.parking;

import com.cyyaw.admin.entity.dto.iot.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "停车场列表查询参数")
public class PkParkingQueryDTO extends PageDTO {

    @Schema(description = "停车场名称模糊关键词")
    private String name;

    @Schema(description = "开放状态(1开放/0不开放)", example = "1")
    private Integer status;
}
