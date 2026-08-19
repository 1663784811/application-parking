package com.cyyaw.admin.entity.dto.user.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户名密码登录")
public class UserLoginRequest extends UserLogin implements Serializable {

    @Schema(description = "用户名", example = "userName")
    private String username;

    @Schema(description = "密码", example = "password")
    private String password;

}
