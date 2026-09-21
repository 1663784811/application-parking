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
     * 微信公众号网页授权链接。scope 不传默认静默授权（只拿 openid，不弹窗）。
     */
    String wechatMpAuthUrl(Long appId, String redirectUri, String state, String scope);

    /**
     * 支付宝登录（没绑过就自动注册）
     */
    LoginRest alipayLogin(UserLoginByAlipayRequest request);

    /**
     * 支付宝网页授权链接。scope 不传默认静默授权（只拿 user_id，不弹窗）。
     */
    String alipayAuthUrl(Long appId, String redirectUri, String state, String scope);

}
