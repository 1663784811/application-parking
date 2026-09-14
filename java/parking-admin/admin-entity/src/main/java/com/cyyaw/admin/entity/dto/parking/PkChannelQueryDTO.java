package com.cyyaw.admin.entity.dto.parking;

import com.cyyaw.admin.entity.dto.iot.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "停车场通道列表查询参数")
public class PkChannelQueryDTO extends PageDTO {

    @Schema(description = "通道类型")
    private String type;

    @Schema(description = "通道状态")
    private Integer status;

    @Schema(description = "停车场ID")
    private Long parkingId;

    @Schema(description = "通道名称或编号模糊关键词")
    private String keyword;
}
