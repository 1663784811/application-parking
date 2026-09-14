package com.cyyaw.admin.entity.dto.member;

import com.cyyaw.admin.entity.dto.iot.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "会员续费记录列表查询参数")
public class MeRenewalRecordQueryDTO extends PageDTO {

    @Schema(description = "车牌号模糊关键词")
    private String plate;

    @Schema(description = "月卡类型")
    private Integer cardType;

    @Schema(description = "续费起始日期(yyyy-MM-dd)")
    private String startTime;

    @Schema(description = "续费结束日期(yyyy-MM-dd)")
    private String endTime;
}
