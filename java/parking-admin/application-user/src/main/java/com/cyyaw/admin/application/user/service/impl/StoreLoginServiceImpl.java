package com.cyyaw.admin.application.user.service.impl;


import cn.hutool.json.JSONObject;
import com.cyyaw.admin.application.user.service.AuStoreAdminService;
import com.cyyaw.admin.application.user.service.AuStoreService;
import com.cyyaw.admin.application.user.service.StoreLoginService;
import com.cyyaw.admin.application.user.service.VerifyCodeService;
import com.cyyaw.admin.common.WebErrCodeEnum;
import com.cyyaw.admin.common.WebException;
import com.cyyaw.admin.config.utils.JwtTokenUtil;
import com.cyyaw.admin.config.utils.RefreshTokenInfo;
import com.cyyaw.admin.entity.dto.user.login.LoginRest;
import com.cyyaw.admin.entity.dto.user.login.StoreLoginRequest;
import com.cyyaw.admin.entity.em.SystemRoleEnum;
import com.cyyaw.admin.entity.module.user.AuStore;
import com.cyyaw.admin.entity.module.user.AuStoreAdmin;
import com.cyyaw.admin.entity.redis.RedisKey;
import com.cyyaw.admin.entity.utils.LoginInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StoreLoginServiceImpl implements StoreLoginService {

    @Autowired
    private AuStoreService auStoreService;


    @Autowired
    private AuStoreAdminService auStoreAdminService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerifyCodeService verifyCodeService;

    @Override
    public LoginRest login(StoreLoginRequest storeLoginRequest) {
        Long storeId = storeLoginRequest.getStoreId();
        String username = storeLoginRequest.getUsername();
        String password = storeLoginRequest.getPassword();
        String phone = storeLoginRequest.getPhone();
        String code = storeLoginRequest.getCode();
        String fingerprint = storeLoginRequest.getFingerprint();
        // 1. 判断验证码( 图片验证码 )
        String key = RedisKey.VERIFY_CODE + ":" + fingerprint;
        if (!verifyCodeService.verifyCode(key, code)) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "验证码错误");
        }
        verifyCodeService.deleteVerifyCode(key);
        // 2.验证密码
        AuStore store = auStoreService.findById(storeId);
        AuStoreAdmin auStoreAdmin = auStoreAdminService.findByAccountAndStoreId(username, store.getId());
        if (!passwordEncoder.matches(password, auStoreAdmin.getPassword())) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "用户名或密码错误");
        }
        return createTokenByStoreAdminId(auStoreAdmin.getId());
    }


    public LoginRest createTokenByStoreAdminId(Long storeAdminId) {
        AuStoreAdmin user = auStoreAdminService.findById(storeAdminId);
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setId(user.getId());
        loginInfo.setAccount(user.getAccount());
        loginInfo.setEnId(user.getEnId());
        loginInfo.setNickName(user.getNickName());
        loginInfo.setAppId(user.getAppId());
        loginInfo.setStoreId(user.getStoreId());
        loginInfo.setSystemRole(SystemRoleEnum.Store.getRole());
        String lgInfo = new JSONObject(loginInfo).toString();
        String token = JwtTokenUtil.createToken(String.valueOf(storeAdminId), lgInfo);
        String refreshToken = JwtTokenUtil.createToken(String.valueOf(storeAdminId), new JSONObject(new RefreshTokenInfo(storeAdminId, SystemRoleEnum.Store.getRole())).toString(), true);
        LoginRest rest = new LoginRest();
        rest.setJwtToken(token);
        rest.setRefreshToken(refreshToken);
        return rest;
    }

}
