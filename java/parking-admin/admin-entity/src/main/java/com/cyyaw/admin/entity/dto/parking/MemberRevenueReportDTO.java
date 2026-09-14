package com.cyyaw.admin.entity.dto.parking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "月卡营收报表查询参数")
public class MemberRevenueReportDTO {

    @Schema(description = "起始日期(yyyy-MM-dd)")
    private String start;

    @Schema(description = "结束日期(yyyy-MM-dd)")
    private String end;

    @Schema(description = "月卡类型")
    private Integer cardType;
}
