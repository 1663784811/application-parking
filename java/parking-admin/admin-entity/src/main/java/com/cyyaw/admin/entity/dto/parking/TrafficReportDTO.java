package com.cyyaw.admin.entity.dto.parking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "车流量报表查询参数")
public class TrafficReportDTO {

    @Schema(description = "停车场ID")
    private Long parkingId;
}
