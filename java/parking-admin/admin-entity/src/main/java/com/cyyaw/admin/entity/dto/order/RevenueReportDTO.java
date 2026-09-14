package com.cyyaw.admin.entity.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "营收报表查询参数")
public class RevenueReportDTO {

    @Schema(description = "起始日期(yyyy-MM-dd)")
    private String start;

    @Schema(description = "结束日期(yyyy-MM-dd)")
    private String end;

    @Schema(description = "停车场ID")
    private Long parkingId;
}
