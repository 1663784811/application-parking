package com.cyyaw.admin.application.user.service;


import com.cyyaw.admin.entity.dto.user.login.*;
import com.cyyaw.admin.entity.module.user.AuUser;
import com.cyyaw.admin.entity.utils.LoginInfo;

public interface AppLoginService {

    /**
     *
     */
    LoginRest login(UserLoginRequest loginRequest);


    LoginRest phoneLogin(UserLoginByPhoneRequest phoneRequest);

    /**
     * 用户注册
     */
    AuUser register(UserRegisterRequest userRegisterRequest);

    /**
     *
     */
    void logout(String token);


    LoginRest createTokenByUserId(Long userId);

    /**
     * 忘记密码
     */
    AuUser forgetPwd(UserForgetPasswordRequest forgetPasswordRequest);

    /**
     * 修改密码
     */
    AuUser updatePassword(LoginInfo userInfo, UserUpdatePasswordRequest updatePasswordRequest);


}
