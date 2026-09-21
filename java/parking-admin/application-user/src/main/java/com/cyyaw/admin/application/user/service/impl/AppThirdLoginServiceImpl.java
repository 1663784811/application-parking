package com.cyyaw.admin.application.user.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.hutool.core.util.StrUtil;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayApiException;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.cyyaw.admin.application.user.service.AppLoginService;
import com.cyyaw.admin.application.user.service.AppThirdLoginService;
import com.cyyaw.admin.application.user.service.AuAppService;
import com.cyyaw.admin.application.user.config.ThirdLoginProperties;
import com.cyyaw.admin.application.user.service.AuUserService;
import com.cyyaw.admin.common.WebErrCodeEnum;
import com.cyyaw.admin.common.WebException;
import com.cyyaw.admin.common.WhyStringUtil;
import com.cyyaw.admin.dao.user.AuUserThirdDao;
import com.cyyaw.admin.entity.dto.user.login.LoginRest;
import com.cyyaw.admin.entity.dto.user.login.UserLoginByAlipayRequest;
import com.cyyaw.admin.entity.dto.user.login.UserLoginByWechatMaRequest;
import com.cyyaw.admin.entity.dto.user.login.UserLoginByWechatMpRequest;
import com.cyyaw.admin.entity.dto.user.login.UserRegisterRequest;
import com.cyyaw.admin.entity.em.ThirdPlatformEnum;
import com.cyyaw.admin.entity.module.user.AuApp;
import com.cyyaw.admin.entity.module.user.AuUser;
import com.cyyaw.admin.entity.module.user.AuUserThird;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.error.WxRuntimeException;
import me.chanjar.weixin.common.service.WxOAuth2Service;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 第三方登录。
 * <p>
 * 三个渠道走同一套逻辑：拿 code 找平台换 openId → 查 au_user_third 有没有绑过 →
 * 没有就用手机验证码登录那套 register() 自动注册一个本地账号，然后复用
 * AppLoginService.createTokenByUserId 签发 token。前端拿到的 LoginRest 跟普通登录完全一样。
 */
@Slf4j
@Service
public class AppThirdLoginServiceImpl implements AppThirdLoginService {

    /** 微信静默授权：不弹窗，只拿 openid。要昵称头像才传 snsapi_userinfo */
    private static final String WECHAT_SCOPE_SILENT = "snsapi_base";
    /** 支付宝静默授权：不弹窗，只拿 user_id。要昵称头像才传 auth_userinfo */
    private static final String ALIPAY_SCOPE_SILENT = "auth_base";

    @Autowired
    private ThirdLoginProperties thirdLoginProperties;

    @Autowired
    private AuUserThirdDao auUserThirdDao;

    @Autowired
    private AuAppService auAppService;

    @Autowired
    private AuUserService auUserService;

    @Autowired
    private AppLoginService appLoginService;

    /**
     * 凭据没配就没有 Bean，用 ObjectProvider 拿，缺失时给明确提示而不是启动就崩
     */
    @Autowired
    private ObjectProvider<WxMaService> wxMaServiceProvider;

    @Autowired
    private ObjectProvider<WxMpService> wxMpServiceProvider;

    @Autowired
    private ObjectProvider<AlipayClient> alipayClientProvider;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginRest wechatMaLogin(UserLoginByWechatMaRequest request) {
        String code = request.getCode();
        AuApp auApp = requireApp(request.getAppId());
        if (StrUtil.isBlank(code)) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "参数错误");
        }
        WxMaService wxMaService = requireWxMaService();
        // 1. code 换 openid
        WxMaJscode2SessionResult session = null;
        try {
            session = wxMaService.getUserService().getSessionInfo(code);
        } catch (WxErrorException | WxRuntimeException e) {
            // 网络不通/DNS 解析失败抛的是 WxRuntimeException，微信返回错误码才是 WxErrorException，
            // 两个都要接住，否则会穿透成 500
            log.warn("微信小程序 jscode2session 失败: {}", e.getMessage());
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "微信登录失败:" + e.getMessage());
        }
        if (session == null || StrUtil.isBlank(session.getOpenid())) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "微信登录失败:未取到openid");
        }
        // 2. 查绑定 / 自动注册
        AuUser user = resolveUser(auApp, request.getStoreId(), ThirdPlatformEnum.WechatMa.getPlatform(),
                session.getOpenid(), session.getUnionid(), request.getNickName(), request.getFace());
        // 3. 签发 token
        return appLoginService.createTokenByUserId(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginRest wechatMpLogin(UserLoginByWechatMpRequest request) {
        String code = request.getCode();
        AuApp auApp = requireApp(request.getAppId());
        if (StrUtil.isBlank(code)) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "参数错误");
        }
        WxMpService wxMpService = requireWxMpService();
        WxOAuth2Service oAuth2Service = wxMpService.getOAuth2Service();
        // 1. code 换 openid
        WxOAuth2AccessToken accessToken = null;
        try {
            accessToken = oAuth2Service.getAccessToken(code);
        } catch (WxErrorException | WxRuntimeException e) {
            log.warn("微信公众号 oauth2 换 token 失败: {}", e.getMessage());
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "微信登录失败:" + e.getMessage());
        }
        if (accessToken == null || StrUtil.isBlank(accessToken.getOpenId())) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "微信登录失败:未取到openid");
        }
        // 2. 昵称头像能拿到就拿，拿不到不影响登录
        String nickName = null;
        String face = null;
        try {
            WxOAuth2UserInfo userInfo = oAuth2Service.getUserInfo(accessToken, "zh_CN");
            if (userInfo != null) {
                nickName = userInfo.getNickname();
                face = userInfo.getHeadImgUrl();
            }
        } catch (WxErrorException | WxRuntimeException e) {
            log.warn("微信公众号取用户信息失败，不阻断登录: {}", e.getMessage());
        }
        // 3. 查绑定 / 自动注册
        AuUser user = resolveUser(auApp, request.getStoreId(), ThirdPlatformEnum.WechatMp.getPlatform(),
                accessToken.getOpenId(), accessToken.getUnionId(), nickName, face);
        // 4. 签发 token
        return appLoginService.createTokenByUserId(user.getId());
    }

    @Override
    public String wechatMpAuthUrl(Long appId, String redirectUri, String state, String scope) {
        requireApp(appId);
        requireRedirectUri(redirectUri);
        WxMpService wxMpService = requireWxMpService();
        // 默认静默授权：出场缴费只要 openid，不需要弹窗去拿昵称头像
        String useScope = StrUtil.isBlank(scope) ? WECHAT_SCOPE_SILENT : scope;
        // WxJava 只编码 redirectUri，state 是原样拼进授权链接的（见 WxOAuth2ServiceImpl#buildAuthorizationUrl），
        // 所以这里必须自己编码 —— 否则目标路由里的 ? & 会把授权链接拆坏
        String safeState = StrUtil.isBlank(state) ? "" : URLEncoder.encode(state, StandardCharsets.UTF_8);
        return wxMpService.getOAuth2Service().buildAuthorizationUrl(redirectUri, useScope, safeState);
    }

    @Override
    public String alipayAuthUrl(Long appId, String redirectUri, String state, String scope) {
        requireApp(appId);
        requireRedirectUri(redirectUri);
        ThirdLoginProperties.Alipay alipay = requireAlipayConfig();
        String useScope = StrUtil.isBlank(scope) ? ALIPAY_SCOPE_SILENT : scope;
        // 支付宝没有 SDK 侧的拼链接方法，按官方格式自己拼（auth_base 静默 / auth_userinfo 弹窗）
        StringBuilder url = new StringBuilder(alipay.getAuthUrl())
                .append("?app_id=").append(alipay.getAppId())
                .append("&scope=").append(useScope)
                .append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
        if (StrUtil.isNotBlank(state)) {
            url.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }
        return url.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginRest alipayLogin(UserLoginByAlipayRequest request) {
        String code = request.getCode();
        AuApp auApp = requireApp(request.getAppId());
        if (StrUtil.isBlank(code)) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "参数错误");
        }
        AlipayClient alipayClient = requireAlipayClient();
        // 1. code 换用户标识
        AlipaySystemOauthTokenRequest tokenRequest = new AlipaySystemOauthTokenRequest();
        tokenRequest.setGrantType("authorization_code");
        tokenRequest.setCode(code);
        AlipaySystemOauthTokenResponse tokenResponse = null;
        try {
            tokenResponse = alipayClient.execute(tokenRequest);
        } catch (AlipayApiException | RuntimeException e) {
            // 网络层异常可能是 RuntimeException（同微信那边），一并接住
            log.warn("支付宝换 token 失败: {}", e.getMessage());
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "支付宝登录失败:" + e.getMessage());
        }
        if (tokenResponse == null || !tokenResponse.isSuccess()) {
            String msg = tokenResponse == null ? "无响应" : tokenResponse.getSubMsg();
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "支付宝登录失败:" + msg);
        }
        // 新版返回 open_id，老应用只返回 user_id
        String openId = tokenResponse.getOpenId();
        if (StrUtil.isBlank(openId)) {
            openId = tokenResponse.getUserId();
        }
        if (StrUtil.isBlank(openId)) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "支付宝登录失败:未取到用户标识");
        }
        // 2. 昵称头像能拿到就拿，拿不到就用前端传的
        String nickName = request.getNickName();
        String face = request.getFace();
        try {
            AlipayUserInfoShareResponse userInfoResponse =
                    alipayClient.execute(new AlipayUserInfoShareRequest(), tokenResponse.getAccessToken());
            if (userInfoResponse != null && userInfoResponse.isSuccess()) {
                if (StrUtil.isNotBlank(userInfoResponse.getNickName())) {
                    nickName = userInfoResponse.getNickName();
                }
                if (StrUtil.isNotBlank(userInfoResponse.getAvatar())) {
                    face = userInfoResponse.getAvatar();
                }
            }
        } catch (AlipayApiException | RuntimeException e) {
            log.warn("支付宝取用户信息失败，不阻断登录: {}", e.getMessage());
        }
        // 3. 查绑定 / 自动注册
        AuUser user = resolveUser(auApp, request.getStoreId(), ThirdPlatformEnum.Alipay.getPlatform(),
                openId, null, nickName, face);
        // 4. 签发 token
        return appLoginService.createTokenByUserId(user.getId());
    }


    /**
     * 查绑定，没有就自动注册。小程序和公众号同一个开放平台账号 unionId 相同，靠它并到同一个人。
     */
    private AuUser resolveUser(AuApp auApp, Long storeId, String platform, String openId, String unionId,
                               String nickName, String face) {
        // 1. 这个渠道的 openid 已经绑过
        AuUserThird third = auUserThirdDao.findByPlatformAndOpenIdAndAppId(platform, openId, auApp.getId());
        if (third != null) {
            AuUser user = auUserService.findUserById(third.getUserId());
            if (user != null) {
                return user;
            }
            // 绑定的用户已经不存在了，重新注册一个并把这条绑定改指过去，避免唯一键冲突
            user = registerUser(auApp, storeId, platform, openId, nickName, face);
            third.setUserId(user.getId());
            auUserThirdDao.save(third);
            return user;
        }
        // 2. unionId 命中 —— 同一个人从小程序/公众号另一个渠道来过
        if (StrUtil.isNotBlank(unionId)) {
            AuUserThird byUnionId = auUserThirdDao.findByUnionIdAndAppId(unionId, auApp.getId());
            if (byUnionId != null) {
                AuUser user = auUserService.findUserById(byUnionId.getUserId());
                if (user != null) {
                    saveThird(auApp, user.getId(), platform, openId, unionId, nickName, face);
                    return user;
                }
            }
        }
        // 3. 第一次来，自动注册
        AuUser user = registerUser(auApp, storeId, platform, openId, nickName, face);
        saveThird(auApp, user.getId(), platform, openId, unionId, nickName, face);
        return user;
    }

    /**
     * 自动注册一个本地账号：账号名用「平台_openid」，密码随机（第三方登录用不到密码）。
     * <p>
     * 密码用 32 位随机串而不是 verifyCode(4)：这个账号名是可以从 openid 推出来的，
     * 而 /app/login/login 是 permitAll 的，4 位纯数字密码只有一万种可能，扛不住爆破。
     */
    private AuUser registerUser(AuApp auApp, Long storeId, String platform, String openId,
                                String nickName, String face) {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername(platform + "_" + openId);
        registerRequest.setPassword(WhyStringUtil.getRandomString(32));
        registerRequest.setPhone(null);
        registerRequest.setEmail(null);
        registerRequest.setCode("");
        registerRequest.setAppId(auApp.getId());
        registerRequest.setStoreId(storeId);
        registerRequest.setFingerprint("");
        AuUser user = appLoginService.register(registerRequest);
        // 平台给了昵称头像就补上（register 默认把昵称设成账号名）
        if (StrUtil.isNotBlank(nickName)) {
            user.setNickName(nickName);
        }
        if (StrUtil.isNotBlank(face)) {
            user.setFace(face);
        }
        if (StrUtil.isNotBlank(nickName) || StrUtil.isNotBlank(face)) {
            user = auUserService.saveUser(user);
        }
        return user;
    }

    private void saveThird(AuApp auApp, Long userId, String platform, String openId, String unionId,
                           String nickName, String face) {
        AuUserThird third = new AuUserThird();
        third.setEnId(auApp.getEnId());
        third.setAppId(auApp.getId());
        third.setUserId(userId);
        third.setPlatform(platform);
        third.setOpenId(openId);
        third.setUnionId(unionId);
        third.setNickName(nickName);
        third.setFace(face);
        auUserThirdDao.save(third);
    }

    private AuApp requireApp(Long appId) {
        if (appId == null) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "参数错误");
        }
        AuApp auApp = auAppService.findAppById(appId);
        if (auApp == null) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "应用不存在");
        }
        return auApp;
    }

    private WxMaService requireWxMaService() {
        WxMaService wxMaService = wxMaServiceProvider.getIfAvailable();
        if (wxMaService == null) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "微信小程序登录未配置");
        }
        return wxMaService;
    }

    private WxMpService requireWxMpService() {
        WxMpService wxMpService = wxMpServiceProvider.getIfAvailable();
        if (wxMpService == null) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "微信公众号登录未配置");
        }
        return wxMpService;
    }

    private AlipayClient requireAlipayClient() {
        AlipayClient alipayClient = alipayClientProvider.getIfAvailable();
        if (alipayClient == null) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "支付宝登录未配置");
        }
        return alipayClient;
    }

    /**
     * 拼授权链接只要 app_id，用不到 AlipayClient；
     * 但先走一遍 requireAlipayClient 判空，"配没配"只留一个判断口径。
     */
    private ThirdLoginProperties.Alipay requireAlipayConfig() {
        requireAlipayClient();
        return thirdLoginProperties.getAlipay();
    }

    private void requireRedirectUri(String redirectUri) {
        if (StrUtil.isBlank(redirectUri)) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "参数错误");
        }
    }

}
