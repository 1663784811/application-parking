package com.cyyaw.admin.entity.dto.parking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "车位列表查询参数")
public class PkSpaceQueryDTO {

    @Schema(description = "停车场ID")
    private Long parkingId;

    @Schema(description = "车位状态")
    private Integer status;

    @Schema(description = "车位类型")
    private Integer type;

    @Schema(description = "车位编号模糊关键词")
    private String keyword;
}
