package com.cyyaw.admin.entity.dto.user.login;

import com.cyyaw.admin.entity.module.user.AuEnterprise;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户登录信息
 */
@Data
public class UserLoginInfoRest<T> {

    @Schema(description = "基本信息", example = "基本信息")
    private T baseInfo;

    @Schema(description = "角色", example = "role")
    private String role;

    @Schema(description = "权限", example = "permission")
    private String permission;

    @Schema(description = "企业信息", example = "enterprise")
    private AuEnterprise auEnterprise;

    // app 信息

    // 门店信息



}
