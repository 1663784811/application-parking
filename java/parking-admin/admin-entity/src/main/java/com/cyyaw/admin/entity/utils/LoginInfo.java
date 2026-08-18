package com.cyyaw.admin.entity.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录信息
 */
@Data
public class LoginInfo {

    @Schema( description = "企业ID", example = "企业ID")
    private Long enId;

    @Schema( description = "APP_ID", example = "appId")
    private Long appId;

    @Schema( description = "store_ID", example = "storeId")
    private Long storeId;

    @Schema( description = "id", example = "id")
    private Long id;

    @Schema( description = "登录账号", example = "userName")
    private String account;

    @Schema( description = "用户名", example = "userName")
    private String nickName;

    @Schema( description = "系统角色", example = "userName")
    private String systemRole;

    @Schema( description = "角色", example = "role")
    private String role;

    @Schema( description = "权限", example = "permission")
    private String permission;


}
