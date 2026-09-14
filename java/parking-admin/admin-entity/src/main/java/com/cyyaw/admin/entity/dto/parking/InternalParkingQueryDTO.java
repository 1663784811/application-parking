package com.cyyaw.admin.entity.dto.parking;

import com.cyyaw.admin.entity.dto.iot.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "内部停车记录列表查询参数")
public class InternalParkingQueryDTO extends PageDTO {

    @Schema(description = "停车场ID")
    private Long parkingId;
}
