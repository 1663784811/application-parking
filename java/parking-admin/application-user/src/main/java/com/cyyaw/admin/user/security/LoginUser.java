package com.cyyaw.admin.user.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 当前登录用户信息（由 JWT 解析后写入 {@link UserContext}）。
 */
@Getter
@AllArgsConstructor
public class LoginUser {

    private final String tid;
    private final String account;
    private final String name;

}
