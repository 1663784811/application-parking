package com.cyyaw.admin.entity.dto.user;

import lombok.Data;

/**
 * 登录请求（storeAdminLogin / login）。
 * <p>
 * storeAdminLogin 时：code 为验证码文本，fingerprint 实际携带 verifyKey（见前端 Login.vue）。
 */
@Data
public class LoginRequest {

    private String username;

    private String password;

    private String code;

    /** 前端实际传的是 verifyKey */
    private String fingerprint;

}
