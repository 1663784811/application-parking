package com.cyyaw.admin.application.user.service;


import com.cyyaw.admin.entity.module.user.AuUser;

public interface AuUserService {


    AuUser findUserByAccountAndAppId(String account, Long appId);

    /**
     * 保存用户
     */
    AuUser saveUser(AuUser user);

    AuUser findUserById(Long id);

    /**
     *
     */
    AuUser findUserByEmailAndAppId(String email, Long appId);

    /**
     *
     */
    AuUser findUserByPhoneAndAppId(String phone, Long appId);
}