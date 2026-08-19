package com.cyyaw.admin.entity.dto.user.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 忘记密码
 */
@Data
public class UserForgetPasswordRequest extends VerifyRequest implements Serializable {

    @Schema(description = "用户名", example = "userName")
    private String username;

    @Schema(description = "手机号", example = "phone")
    private String phone;

    @Schema(description = "邮箱", example = "email")
    private String email;

    @Schema( description = "验证码", example = "code")
    private String code;

    @Schema(description = "新密码", example = "password")
    private String password;

    @Schema( description = "appId", example = "appId")
    private Long appId;
}
