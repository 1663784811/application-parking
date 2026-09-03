package com.cyyaw.application.parking.gate.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 返回前端的用户信息（不含密码）。
 */
@Data
public class UserInfo {

    @Schema(description = "用户id")
    private Integer id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "所属车场名称")
    private String parkingName;

    @Schema(description = "所属车场id")
    private Integer parkingId;
}
