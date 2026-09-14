package com.cyyaw.admin.entity.dto.user;

import com.cyyaw.admin.entity.dto.iot.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "管理员列表查询参数")
public class AuAdminQueryDTO extends PageDTO {

    @Schema(description = "账号、真实姓名或手机号模糊关键词")
    private String keyword;

    @Schema(description = "状态")
    private Integer status;
}
