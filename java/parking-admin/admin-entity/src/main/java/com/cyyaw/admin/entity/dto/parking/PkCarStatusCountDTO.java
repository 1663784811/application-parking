package com.cyyaw.admin.entity.dto.parking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "在场车辆数查询参数")
public class PkCarStatusCountDTO {

    @Schema(description = "车辆状态（默认0）")
    private Integer status = 0;
}
