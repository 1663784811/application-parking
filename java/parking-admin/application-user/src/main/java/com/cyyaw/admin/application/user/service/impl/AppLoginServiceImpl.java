package com.cyyaw.admin.application.user.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cyyaw.admin.application.user.service.*;
import com.cyyaw.admin.common.WebErrCodeEnum;
import com.cyyaw.admin.common.WebException;
import com.cyyaw.admin.common.WhyStringUtil;
import com.cyyaw.admin.config.utils.JwtTokenUtil;
import com.cyyaw.admin.config.utils.RefreshTokenInfo;
import com.cyyaw.admin.entity.dto.user.login.*;
import com.cyyaw.admin.entity.em.SystemRoleEnum;
import com.cyyaw.admin.entity.module.user.AuApp;
import com.cyyaw.admin.entity.module.user.AuStore;
import com.cyyaw.admin.entity.module.user.AuUser;
import com.cyyaw.admin.entity.redis.RedisKey;
import com.cyyaw.admin.entity.utils.LoginInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AppLoginServiceImpl implements AppLoginService {

    @Autowired
    private AuUserService auUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuAppService auAppService;

    @Autowired
    private AuStoreService auStoreService;

    @Autowired
    private VerifyCodeService verifyCodeService;


    @Override
    public LoginRest login(UserLoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        Long appId = loginRequest.getAppId();
        String code = loginRequest.getCode();
        String fingerprint = loginRequest.getFingerprint();

        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "参数错误");
        }
//        // 1. 判断验证码
//        if () {
//            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "验证码错误");
//        }
        // 2. 查询数据库
        AuApp auApp = auAppService.findAppById(appId);
        if (auApp == null) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "应用不存在");
        }
        AuUser user = auUserService.findUserByAccountAndAppId(username, appId);
        if (user == null) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "用户名或密码错误");
        }
        // 3. 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "用户名或密码错误");
        }
        // 4. 生成 token
        return createTokenByUserId(user.getId());
    }

    @Override
    public LoginRest phoneLogin(UserLoginByPhoneRequest phoneRequest) {
        String phone = phoneRequest.getPhone();
        String code = phoneRequest.getCode();
        Long appId = phoneRequest.getAppId();
        Long storeId = phoneRequest.getStoreId();
        String fingerprint = phoneRequest.getFingerprint();
        if (StrUtil.isBlank(fingerprint) || StrUtil.isBlank(code) || StrUtil.isBlank(phone) || appId == null) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "参数错误");
        }
        //  1. 判断验证码
        if (!verifyCodeService.verifyCode(RedisKey.VERIFY_CODE + ":" + phone + ":" + fingerprint, code)) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "验证码错误");
        }
        if (null != storeId) {
            AuStore store = auStoreService.findById(storeId);
            if (store == null) {
                WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "门店不存在");
            }
        }

        verifyCodeService.deleteVerifyCode(RedisKey.VERIFY_CODE + ":" + phone + ":" + fingerprint);
        // 2. 查询数据库
        AuApp auApp = auAppService.findAppById(appId);
        if (auApp == null) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "应用不存在");
        }
        AuUser user = auUserService.findUserByPhoneAndAppId(phone, appId);
        if (user == null) {
            // 注册账号
            UserRegisterRequest registerRequest = new UserRegisterRequest();
            registerRequest.setUsername(phone);
            registerRequest.setPassword(WhyStringUtil.verifyCode(4));
            registerRequest.setPhone(phone);
            registerRequest.setCode("");
            registerRequest.setEmail("");
            registerRequest.setAppId(appId);
            registerRequest.setStoreId(storeId);
            registerRequest.setFingerprint("");
            user = register(registerRequest, false);
        }
        user.setLastLoginTime(LocalDateTime.now());
        auUserService.saveUser(user);
        // 4. 生成 token
        return createTokenByUserId(user.getId());
    }


    @Override
    public AuUser register(UserRegisterRequest userRegisterRequest) {
        return register(userRegisterRequest, true);
    }

    public AuUser register(UserRegisterRequest userRegisterRequest, boolean verify) {
        String code = userRegisterRequest.getCode();
        String username = userRegisterRequest.getUsername();
        String password = userRegisterRequest.getPassword();
        String email = userRegisterRequest.getEmail();
        String phone = userRegisterRequest.getPhone();
        Long appId = userRegisterRequest.getAppId();
        // 验证验证码
        if (verify) {


        }
        // 查询数据库
        AuApp auApp = auAppService.findAppById(appId);
        if (auApp == null) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "应用不存在");
        }
        AuUser auUser = auUserService.findUserByAccountAndAppId(username, appId);
        if (auUser != null) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "账号已经存在");
        }
        auUser = auUserService.findUserByPhoneAndAppId(phone, appId);
        if (auUser != null) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "手机号已经被注册");
        }
        if(StrUtil.isNotBlank(email)){
            auUser = auUserService.findUserByEmailAndAppId(email, appId);
            if (auUser != null) {
                WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "邮箱已经被注册");
            }
        }
        Long storeId = null;
        if (userRegisterRequest.getStoreId() != null) {
            AuStore store = auStoreService.findById(userRegisterRequest.getStoreId());
            if (store == null) {
                WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "门店不存在");
            }
            if (!store.getAppId().equals(appId)) {
                WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "非法操作");
            }
            storeId = store.getId();
        }
        // 保存数据
        AuUser user = new AuUser();
        user.setAppId(auApp.getId());
        user.setEnId(auApp.getEnId());
        user.setStoreId(storeId);
        user.setNickName(username);
        user.setAccount(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFace(null);
        user.setRealName(null);
        user.setGender(0);
        user.setBirthday(null);
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus(1);
        user.setLastLoginTime(null);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return auUserService.saveUser(user);
    }


    @Override
    public void logout(String token) {

    }

    @Override
    public LoginRest createTokenByUserId(Long userId) {
        AuUser user = auUserService.findUserById(userId);
        user.setLastLoginTime(LocalDateTime.now());
        auUserService.saveUser(user);
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setId(user.getId());
        loginInfo.setAccount(user.getAccount());
        loginInfo.setEnId(user.getEnId());
        loginInfo.setNickName(user.getNickName());
        loginInfo.setAppId(user.getAppId());
        loginInfo.setSystemRole(SystemRoleEnum.User.getRole());
        String lgInfo = new JSONObject(loginInfo).toString();
        String token = JwtTokenUtil.createToken(String.valueOf(userId), lgInfo);
        String refreshToken = JwtTokenUtil.createToken(String.valueOf(userId), new JSONObject(new RefreshTokenInfo(userId, SystemRoleEnum.User.getRole())).toString(), true);
        LoginRest rest = new LoginRest();
        rest.setJwtToken(token);
        rest.setRefreshToken(refreshToken);
        return rest;
    }


    @Override
    public AuUser forgetPwd(UserForgetPasswordRequest forgetPasswordRequest) {
        Long appId = forgetPasswordRequest.getAppId();
        AuApp auApp = auAppService.findAppById(appId);
        if (auApp == null) WebException.fail(WebErrCodeEnum.WEB_ILLEGALSTATE);
        Long enId = auApp.getEnId();
        String username = forgetPasswordRequest.getUsername();
        String phone = forgetPasswordRequest.getPhone();
        String code = forgetPasswordRequest.getCode();
        String password = forgetPasswordRequest.getPassword();
        String email = forgetPasswordRequest.getEmail();
        String fingerprint = forgetPasswordRequest.getFingerprint();
        if (StrUtil.isNotBlank(phone)) {
            // 验证验证码
            String key = RedisKey.VERIFY_CODE + ":" + phone + ":" + fingerprint;
            if (!verifyCodeService.verifyCode(key, code)) {
                verifyCodeService.deleteVerifyCode(key);
                // 通过手机号重置密码
                AuUser auUser = auUserService.findUserByPhoneAndAppId(phone, appId);
                auUser.setPassword(passwordEncoder.encode(password));
                return auUserService.saveUser(auUser);
            }
        } else if (StrUtil.isNotBlank(email)) {
            // 验证验证码
            String key = RedisKey.VERIFY_CODE + ":" + email + ":" + fingerprint;
            if (!verifyCodeService.verifyCode(key, code)) {
                verifyCodeService.deleteVerifyCode(key);
                // 通过邮箱重置密码
                AuUser auUser = auUserService.findUserByEmailAndAppId(email, appId);
                auUser.setPassword(passwordEncoder.encode(password));
                return auUserService.saveUser(auUser);
            }
        }
        return null;
    }


    @Override
    public AuUser updatePassword(LoginInfo userInfo, UserUpdatePasswordRequest updatePasswordRequest) {
        String oldPassword = updatePasswordRequest.getOldPassword();
        String nowPassword = updatePasswordRequest.getNowPassword();
        AuUser auUser = auUserService.findUserById(userInfo.getId());
        if (!passwordEncoder.matches(oldPassword, auUser.getPassword())) {
            WebException.fail(WebErrCodeEnum.WEB_ERR, "新密码与原密码不匹配");
        }
        auUser.setPassword(passwordEncoder.encode(nowPassword));
        return auUserService.saveUser(auUser);
    }


}
