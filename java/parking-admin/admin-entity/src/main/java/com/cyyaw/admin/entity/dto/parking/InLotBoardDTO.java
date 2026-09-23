package com.cyyaw.admin.entity.dto.parking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "在场车辆看板查询参数")
public class InLotBoardDTO {

    @Schema(description = "停车场ID")
    private Long parkingId;

    @Schema(description = "车牌号（模糊匹配，可空）")
    private String carNumber;
}
