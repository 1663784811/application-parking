package com.cyyaw.admin.user.service;

import com.cyyaw.admin.entity.dto.user.LoginResult;
import com.cyyaw.admin.entity.dto.user.RegisterRequest;
import com.cyyaw.admin.entity.dto.user.UserInfoVo;

/**
 * 用户 / 登录 / token 业务接口。
 */
public interface UserService {

    /** 账号密码登录（无验证码） */
    LoginResult login(String username, String password);

    /** 后台登录（带验证码，fingerprint 实际为 verifyKey） */
    LoginResult storeAdminLogin(String username, String password, String code, String verifyKey);

    /** 注册 */
    void register(RegisterRequest request);

    /** 用 refreshToken 换新 token 对（轮转） */
    LoginResult refresh(String refreshToken);

    /** 获取当前登录用户信息（依赖 UserContext） */
    UserInfoVo findUserInfo();

}
