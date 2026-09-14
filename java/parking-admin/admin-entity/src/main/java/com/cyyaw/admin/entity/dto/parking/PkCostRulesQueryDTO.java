package com.cyyaw.admin.entity.dto.parking;

import com.cyyaw.admin.entity.dto.iot.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "计费规则列表查询参数")
public class PkCostRulesQueryDTO extends PageDTO {

    @Schema(description = "计费规则名称模糊关键词")
    private String name;
}
