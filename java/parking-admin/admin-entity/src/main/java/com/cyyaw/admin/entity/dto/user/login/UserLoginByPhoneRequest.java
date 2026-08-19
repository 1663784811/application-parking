package com.cyyaw.admin.entity.dto.user.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户手机验证码登录")
public class UserLoginByPhoneRequest extends UserLogin implements Serializable {
    @Schema(description = "手机号", example = "phone")
    private String phone;
}
