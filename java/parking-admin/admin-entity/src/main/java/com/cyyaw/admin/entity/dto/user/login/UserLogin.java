package com.cyyaw.admin.entity.dto.user.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户登录")
public class UserLogin extends VerifyRequest implements Serializable {

    @Schema(description = "验证码", example = "123456")
    private String code;

    @Schema(description = "appId", example = "111")
    private Long appId;

    @Schema(description = "门店ID", example = "222")
    private Long storeId;
}
