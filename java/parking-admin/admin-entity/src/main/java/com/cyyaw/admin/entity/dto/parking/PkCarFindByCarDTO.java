package com.cyyaw.admin.entity.dto.parking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "按车牌查询在场车辆参数")
public class PkCarFindByCarDTO {

    @Schema(description = "停车场ID")
    private Long parkingId;

    @Schema(description = "车牌号")
    private String carNumber;
}
