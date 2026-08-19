package com.cyyaw.admin.entity.dto.user.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 注册
 */
@Data
public class UserRegisterRequest extends VerifyRequest implements Serializable {

    @Schema( description = "用户名", example = "userName")
    private String username;

    @Schema( description = "密码", example = "password")
    private String password;

    @Schema( description = "手机号", example = "phone")
    private String phone;

    @Schema( description = "code", example = "code")
    private String code;

    @Schema( description = "email", example = "email")
    private String email;

    @Schema( description = "appId", example = "appId")
    private Long appId;

    @Schema( description = "storeId", example = "storeId")
    private Long storeId;
}
