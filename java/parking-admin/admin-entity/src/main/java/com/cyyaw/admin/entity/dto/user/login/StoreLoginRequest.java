package com.cyyaw.admin.entity.dto.user.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class StoreLoginRequest extends VerifyRequest implements Serializable {

    @Schema(description = "门店ID", example = "storeId")
    private Long storeId;

    @Schema(description = "用户名", example = "userName")
    private String username;

    @Schema(description = "密码", example = "password")
    private String password;

    @Schema(description = "手机号", example = "phone")
    private String phone;

    @Schema(description = "code", example = "code")
    private String code;

}
