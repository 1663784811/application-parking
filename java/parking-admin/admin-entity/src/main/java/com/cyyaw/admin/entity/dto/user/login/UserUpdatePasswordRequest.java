package com.cyyaw.admin.entity.dto.user.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 忘记密码
 */
@Data
public class UserUpdatePasswordRequest implements Serializable {


    @Schema( description = "原密码", example = "oldPassword")
    private String oldPassword;

    @Schema(description = "新密码", example = "nowPassword")
    private String nowPassword;


}
