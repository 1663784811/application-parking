package com.cyyaw.admin.application.user.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cyyaw.admin.application.user.service.*;
import com.cyyaw.admin.common.WebErrCodeEnum;
import com.cyyaw.admin.common.WebException;
import com.cyyaw.admin.config.utils.JwtTokenUtil;
import com.cyyaw.admin.config.utils.RefreshTokenInfo;
import com.cyyaw.admin.entity.dto.user.login.*;
import com.cyyaw.admin.entity.em.SystemRoleEnum;
import com.cyyaw.admin.entity.module.user.AuAdmin;
import com.cyyaw.admin.entity.module.user.AuEnterprise;
import com.cyyaw.admin.entity.redis.RedisKey;
import com.cyyaw.admin.entity.utils.LoginInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminLoginServiceImpl implements AdminLoginService {

    @Autowired
    private AuAdminService auAdminService;

    @Autowired
    private AuEnterpriseService auEnterpriseService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerifyCodeService verifyCodeService;

    @Autowired
    private AuMenuService auMenuService;

    @Autowired
    private ConfSysService confSysService;



    @Override
    @Transactional(readOnly = true)
    public LoginRest login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String code = loginRequest.getCode();
        String password = loginRequest.getPassword();
        String fingerprint = loginRequest.getFingerprint();
        Long enId = loginRequest.getEnId();
        if (StrUtil.isBlank(username) || StrUtil.isBlank(code)) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "参数错误");
        }
        AuEnterprise enterprise = auEnterpriseService.findEnterpriseById(enId);
        if (enterprise == null) {
            WebException.fail(WebErrCodeEnum.WEB_AUTHENTICATION_ERR);
        }
        // 1. 判断验证码( 图片验证码 )
        String key = RedisKey.VERIFY_CODE + ":" + fingerprint;
        if (!verifyCodeService.verifyCode(key, code)) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "验证码错误");
        }
        verifyCodeService.deleteVerifyCode(key);
        // 2. 查询数据库
        AuAdmin admin = auAdminService.findAdminByAccountAndEnId(username, enterprise.getId());
        if (admin == null) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "用户名或密码错误");
        }
        // 3. 验证密码
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "用户名或密码错误");
        }
        // 4. 生成 token
        return createTokenByAdminId(admin.getId());
    }

    @Transactional
    @Override
    public AuAdmin register(AdminRegisterRequest adminRegisterRequest) {
        String username = adminRegisterRequest.getUsername();
        String password = adminRegisterRequest.getPassword();
        String phone = adminRegisterRequest.getPhone();
        String code = adminRegisterRequest.getCode();
        String fingerprint = adminRegisterRequest.getFingerprint();
        Long enId = adminRegisterRequest.getEnId();
        // 1. 验证码校验
        if (!StringUtils.hasText(code)) {
            throw new RuntimeException("验证码不能为空");
        }
        String key = RedisKey.phoneVerifyCodeKey(phone, fingerprint);
        if (!verifyCodeService.verifyCode(key, code)) {
            WebException.fail(WebErrCodeEnum.WEB_LOGINERR, "验证码错误");
        }
        // 2. 查询数据库
        AuEnterprise enterprise = auEnterpriseService.findEnterpriseById(enId);
        if (enterprise == null) {
            WebException.fail(WebErrCodeEnum.WEB_REGISTER_ERR, "企业不存在");
        }
        AuAdmin tempAdmin = auAdminService.findAdminByAccountAndEnId(username, enterprise.getId());
        if (tempAdmin != null) {
            WebException.fail(WebErrCodeEnum.WEB_REGISTER_ERR, "账号已被注册");
        }
        // 3. 创建新用户
        AuAdmin auAdmin = new AuAdmin();
        auAdmin.setEnId(enterprise.getId());
        auAdmin.setAccount(username);
        auAdmin.setNickName(username);
        auAdmin.setPhone(phone);
        auAdmin.setPassword(passwordEncoder.encode(password));
        AuAdmin admin = auAdminService.saveAdmin(auAdmin);
        // 4. 分配默认角色和权限


        return admin;
    }

    @Override
    @Transactional(readOnly = true)
    public AuAdmin findAdminInfo(String token) {
        if (!StringUtils.hasText(token)) {
            throw new RuntimeException("token不能为空");
        }

        // 1. 验证 token
        if (!JwtTokenUtil.verifierToken(token)) {
            throw new RuntimeException("无效的token");
        }

        // 2. 获取用户信息
        String username = JwtTokenUtil.getSubjectFromToken(token);
//        AuAdmin admin = auAdminService.findByAdminId(username);
        AuAdmin admin = null;

        if (admin == null) {
            throw new RuntimeException("用户不存在");
        }

        return admin;
    }

    @Override
    public void logout(String token) {
        if (!StringUtils.hasText(token)) {
            throw new RuntimeException("token不能为空");
        }

        // 1. 验证 token
        if (!JwtTokenUtil.verifierToken(token)) {
            throw new RuntimeException("无效的token");
        }

        // 2. 使 token 失效
//        jwtTokenUtil.invalidateToken(token);
    }

    @Transactional
    @Override
    public RegisterEnterpriseRest registerEnterprise(RegisterEnterpriseRequest loginRequest) {
        String name = loginRequest.getName();
        String logo = loginRequest.getLogo();

        String person = loginRequest.getPerson();
        String phone = loginRequest.getPhone();
        String password = loginRequest.getPassword();
        String code = loginRequest.getCode();
        String fingerprint = loginRequest.getFingerprint();
        // ===========================================   验证数据
        AuEnterprise enterprisePhone = auEnterpriseService.findEnterpriseByPhone(phone);
        if (enterprisePhone != null) {
            throw new RuntimeException("手机号已经被注册！");
        }
        String eCode = confSysService.createNewCode();
        AuEnterprise enterprise = auEnterpriseService.findEnterpriseByCode(eCode);
        if (enterprise != null) {
            throw new RuntimeException("企业编号已经存在");
        }
        // ===========================================
        AuEnterprise ent = new AuEnterprise();
        ent.setName(name);
        ent.setLogo(logo);
        ent.setCode(eCode);
        ent.setPerson(person);
        ent.setPhone(phone);
        ent.setRootAccount(phone);
        AuEnterprise en = auEnterpriseService.saveEnterprise(ent);
        // 注册管理员
        AdminRegisterRequest admin = new AdminRegisterRequest();
        admin.setUsername(phone);
        admin.setPassword(password);
        admin.setPhone(phone);
        admin.setCode(code);
        admin.setFingerprint(fingerprint);
        admin.setEnId(en.getId());
        AuAdmin register = register(admin);
        // 生成初始化菜单
        JSONObject params = new JSONObject();
        params.put("eCode", en.getCode());
        auMenuService.initMenu(en.getId(), "classpath:menu/admin.json", SystemRoleEnum.Admin.getRole(),null, params.toString(), 0);
        RegisterEnterpriseRest rest = new RegisterEnterpriseRest();
        rest.setName(en.getName());
        rest.setLogo(en.getLogo());
        rest.setECode(en.getCode());
        rest.setPerson(en.getPerson());
        rest.setPhone(en.getPhone());
        rest.setUsername(register.getAccount());
        rest.setId(en.getId());
        return rest;
    }

    @Override
    public LoginRest createTokenByAdminId(Long adminId) {
        AuAdmin admin = auAdminService.findByAdminId(adminId);
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setAccount(admin.getAccount());
        loginInfo.setId(admin.getId());
        loginInfo.setEnId(admin.getEnId());
        loginInfo.setSystemRole(SystemRoleEnum.Admin.getRole());
        String lgInfo = new JSONObject(loginInfo).toString();
        String token = JwtTokenUtil.createToken(String.valueOf(adminId), lgInfo);
        String refreshToken = JwtTokenUtil.createToken(String.valueOf(adminId), new JSONObject(new RefreshTokenInfo(adminId, SystemRoleEnum.Admin.getRole())).toString(), true);
        LoginRest rest = new LoginRest();
        rest.setJwtToken(token);
        rest.setRefreshToken(refreshToken);
        return rest;
    }
} 