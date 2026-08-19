package com.cyyaw.admin.entity.redis;

public class RedisKey {

    // 验证码
    public static final String VERIFY_CODE = "verifyCode";
    // 5分钟过期
    public static final long VERIFY_CODE_EXPIRATION = 1000 * 60 * 5;

    public static final String phoneVerifyCodeKey(String phone, String fingerprint) {
        return RedisKey.VERIFY_CODE + ":" + phone + ":" + fingerprint;
    }

}
