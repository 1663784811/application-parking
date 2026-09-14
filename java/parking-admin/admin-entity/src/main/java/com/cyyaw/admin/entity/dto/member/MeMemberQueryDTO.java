package com.cyyaw.admin.entity.dto.member;

import com.cyyaw.admin.entity.dto.iot.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "会员列表查询参数")
public class MeMemberQueryDTO extends PageDTO {

    @Schema(description = "车牌号模糊关键词")
    private String plate;

    @Schema(description = "手机号模糊关键词")
    private String phone;

    @Schema(description = "会员状态(1正常 2即将到期 3已过期 4已冻结)")
    private Integer status;
}
