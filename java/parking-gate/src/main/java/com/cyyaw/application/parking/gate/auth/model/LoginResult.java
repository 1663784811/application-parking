package com.cyyaw.application.parking.gate.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResult {

    @Schema(description = "登录令牌（后续请求放入 Authorization: Bearer <token>）")
    private String token;

    @Schema(description = "当前用户信息")
    private UserInfo userInfo;
}
