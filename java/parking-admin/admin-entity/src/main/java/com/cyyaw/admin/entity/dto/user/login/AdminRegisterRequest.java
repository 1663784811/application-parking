package com.cyyaw.admin.entity.dto.user.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class AdminRegisterRequest extends VerifyRequest implements Serializable {

    @Schema( description = "企业ID", example = "enId")
    private Long enId;

    @Schema( description = "userName", example = "userName")
    private String username;

    @Schema( description = "password", example = "password")
    private String password;

    @Schema( description = "phone", example = "phone")
    private String phone;

    @Schema( description = "code", example = "code")
    private String code;      // 验证码

}
