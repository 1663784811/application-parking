package com.cyyaw.admin.application.user.controller.login;

import com.cyyaw.admin.application.user.service.AppThirdLoginService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.user.login.LoginRest;
import com.cyyaw.admin.entity.dto.user.login.UserLoginByAlipayRequest;
import com.cyyaw.admin.entity.dto.user.login.UserLoginByWechatMaRequest;
import com.cyyaw.admin.entity.dto.user.login.UserLoginByWechatMpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "用户登录模块-第三方登录")
@RequestMapping("/app/login")
public class ThirdLoginController {

    @Autowired
    private AppThirdLoginService appThirdLoginService;

    @PostMapping("/wechatMaLogin")
    @Operation(summary = "用户登录-微信小程序登录(或注册)", description = "微信小程序登录(或注册)。code 由 wx.login 获取")
    public BaseResult<LoginRest> wechatMaLogin(@RequestBody UserLoginByWechatMaRequest request) {
        LoginRest login = appThirdLoginService.wechatMaLogin(request);
        return BaseResult.ok(login, "登录成功");
    }

    @PostMapping("/wechatMpLogin")
    @Operation(summary = "用户登录-微信公众号登录(或注册)", description = "微信公众号网页授权登录(或注册)。code 由网页授权回调带回")
    public BaseResult<LoginRest> wechatMpLogin(@RequestBody UserLoginByWechatMpRequest request) {
        LoginRest login = appThirdLoginService.wechatMpLogin(request);
        return BaseResult.ok(login, "登录成功");
    }

    @GetMapping("/wechatMpAuthUrl")
    @Operation(summary = "获取微信公众号网页授权链接", description = "redirectUri 传前端回调页面地址，微信会带 code 回跳；scope 不传默认 snsapi_base 静默授权")
    public BaseResult<String> wechatMpAuthUrl(Long appId, String redirectUri, String state, String scope) {
        String url = appThirdLoginService.wechatMpAuthUrl(appId, redirectUri, state, scope);
        return BaseResult.ok(url);
    }

    @PostMapping("/alipayLogin")
    @Operation(summary = "用户登录-支付宝登录(或注册)", description = "支付宝登录(或注册)。code 传授权回调带回的 auth_code")
    public BaseResult<LoginRest> alipayLogin(@RequestBody UserLoginByAlipayRequest request) {
        LoginRest login = appThirdLoginService.alipayLogin(request);
        return BaseResult.ok(login, "登录成功");
    }

    @GetMapping("/alipayAuthUrl")
    @Operation(summary = "获取支付宝网页授权链接", description = "redirectUri 传前端回调页面地址，支付宝会带 auth_code 回跳；scope 不传默认 auth_base 静默授权")
    public BaseResult<String> alipayAuthUrl(Long appId, String redirectUri, String state, String scope) {
        String url = appThirdLoginService.alipayAuthUrl(appId, redirectUri, state, scope);
        return BaseResult.ok(url);
    }

}
