package com.cyyaw.admin.entity.dto.user;

import com.cyyaw.admin.entity.dto.iot.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "系统日志列表查询参数")
public class AuLogQueryDTO extends PageDTO {

    @Schema(description = "日志类型")
    private String logType;

    @Schema(description = "起始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "操作人账号或描述模糊关键词")
    private String keyword;
}
