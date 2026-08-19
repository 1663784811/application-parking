package com.cyyaw.admin.application.user.service;


import com.cyyaw.admin.entity.dto.user.login.*;
import com.cyyaw.admin.entity.module.user.AuAdmin;

public interface AdminLoginService {

    /**
     * 管理员登录
     *
     * @param loginRequest 登录请求
     * @return 管理员信息
     */
    LoginRest login(LoginRequest loginRequest);

    /**
     * 管理员注册
     *
     */
    AuAdmin register(AdminRegisterRequest adminRegisterRequest);

    /**
     * 获取管理员信息
     *
     * @param token JWT token
     * @return 管理员信息
     */
    AuAdmin findAdminInfo(String token);

    /**
     * 退出登录
     *
     * @param token JWT token
     */
    void logout(String token);


    /**
     * 注册企业
     */
    RegisterEnterpriseRest registerEnterprise(RegisterEnterpriseRequest loginRequest);


    /**
     * 生成token信息
     */
    LoginRest createTokenByAdminId(Long adminId);


}
