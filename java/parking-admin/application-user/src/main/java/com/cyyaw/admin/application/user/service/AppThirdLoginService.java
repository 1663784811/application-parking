package com.cyyaw.admin.application.user.service;

import com.cyyaw.admin.entity.dto.user.login.LoginRest;
import com.cyyaw.admin.entity.dto.user.login.UserLoginByAlipayRequest;
import com.cyyaw.admin.entity.dto.user.login.UserLoginByWechatMaRequest;
import com.cyyaw.admin.entity.dto.user.login.UserLoginByWechatMpRequest;

/**
 * 第三方登录（微信小程序 / 微信公众号 / 支付宝）
 */
public interface AppThirdLoginService {

    /**
     * 微信小程序登录（没绑过就自动注册）
     */
    LoginRest wechatMaLogin(UserLoginByWechatMaRequest request);

    /**
     * 微信公众号网页授权登录（没绑过就自动注册）
     */
    LoginRest wechatMpLogin(UserLoginByWechatMpRequest request);

    /**
     * 微信公众号网页授权链接
     */
    String wechatMpAuthUrl(Long appId, String redirectUri, String state);

    /**
     * 支付宝登录（没绑过就自动注册）
     */
    LoginRest alipayLogin(UserLoginByAlipayRequest request);

}
