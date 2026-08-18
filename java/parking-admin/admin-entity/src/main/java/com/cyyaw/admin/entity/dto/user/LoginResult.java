package com.cyyaw.admin.entity.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录 / 刷新成功响应 data：{jwtToken, refreshToken}。
 */
@Data
@AllArgsConstructor
public class LoginResult {

    private String jwtToken;

    private String refreshToken;

}
