package com.cyyaw.admin.application.user.controller.login;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cyyaw.admin.application.user.service.*;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.config.utils.JwtTokenUtil;
import com.cyyaw.admin.config.utils.RefreshTokenInfo;
import com.cyyaw.admin.entity.dto.user.login.LoginRest;
import com.cyyaw.admin.entity.dto.user.login.TokenRequest;
import com.cyyaw.admin.entity.dto.user.login.UserLoginInfoRest;
import com.cyyaw.admin.entity.em.SystemRoleEnum;
import com.cyyaw.admin.entity.module.user.AuAdmin;
import com.cyyaw.admin.entity.module.user.AuEnterprise;
import com.cyyaw.admin.entity.module.user.AuStoreAdmin;
import com.cyyaw.admin.entity.module.user.AuUser;
import com.cyyaw.admin.entity.utils.LoginInfo;
import com.cyyaw.admin.entity.utils.LoginInfoContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "token")
@RequestMapping("/common/token")
public class TokenController {

    @Autowired
    public AdminLoginService adminLoginService;

    @Autowired
    private AppLoginService appLoginService;

    @Autowired
    private AuAdminService auAdminService;

    @Autowired
    private AuUserService auUserService;

    @Autowired
    private AuRoleService auRoleService;

    @Autowired
    private AuPermissionService auPermissionService;

    @Autowired
    private AuEnterpriseService auEnterpriseService;

    @Autowired
    private AuStoreAdminService auStoreAdminService;

    @Autowired
    private StoreLoginService storeLoginService;


    @Operation(summary = "刷新token", description = "刷新token")
    @PostMapping("/refreshToken")
    public BaseResult refreshToken(@RequestBody TokenRequest loginRequest) {
        LoginRest rest = new LoginRest();
        String refreshToken = loginRequest.getRefreshToken();
        String json = JwtTokenUtil.getTokenData(refreshToken.replace(JwtTokenUtil.TOKEN_PREFIX, ""));
        RefreshTokenInfo refreshTokenInfo = new JSONObject(json).toBean(RefreshTokenInfo.class);
        String role = refreshTokenInfo.getRole();
        Long id = refreshTokenInfo.getId();
        if (role.equals(SystemRoleEnum.Admin.getRole())) {
            rest = adminLoginService.createTokenByAdminId(id);
        } else if (role.equals(SystemRoleEnum.User.getRole())) {
            rest = appLoginService.createTokenByUserId(id);
        } else if (role.equals(SystemRoleEnum.Store.getRole())) {
            rest = storeLoginService.createTokenByStoreAdminId(id);
        } else if (role.equals(SystemRoleEnum.Root.getRole())) {

        }
        if (StrUtil.isNotBlank(rest.getJwtToken())) {
            return BaseResult.ok(rest, "成功");
        } else {
            return BaseResult.fail();
        }
    }


    @Operation(summary = "获取登录信息", description = "获取登录信息")
    @GetMapping("/findUserInfo")
    public BaseResult<UserLoginInfoRest<Object>> findAdminInfo() {
        LoginInfo loginInfo = LoginInfoContext.getLoginInfo();
        String role = loginInfo.getSystemRole();
        Long id = loginInfo.getId();
        Long enId = loginInfo.getEnId();
        UserLoginInfoRest<Object> rest = new UserLoginInfoRest<>();
        if (role.equals(SystemRoleEnum.Admin.getRole())) {
            // 用户
            AuAdmin admin = auAdminService.findByAdminId(id);
            // 查角色
            String adminRole = auRoleService.findAdminRoleByAdminId(id);
            // 查权限
            String permission = auPermissionService.findPermissionByAdminId(id);
            // 查企业
            AuEnterprise auEnterprise = auEnterpriseService.findEnterpriseById(enId);
            rest.setBaseInfo(admin);
            rest.setRole(role);
            rest.setPermission(permission);
            rest.setAuEnterprise(auEnterprise);
        } else if (role.equals(SystemRoleEnum.User.getRole())) {
            AuUser user = auUserService.findUserById(id);
            rest.setRole(role);
            rest.setBaseInfo(user);
        } else if (role.equals(SystemRoleEnum.Root.getRole())) {

        } else if (role.equals(SystemRoleEnum.Store.getRole())) {
            AuStoreAdmin auStoreAdmin = auStoreAdminService.findById(id);
            // 查企业
            AuEnterprise auEnterprise = auEnterpriseService.findEnterpriseById(enId);
            rest.setBaseInfo(auStoreAdmin);
            rest.setAuEnterprise(auEnterprise);
            rest.setRole(role);
        }
        return BaseResult.ok(rest);
    }


}
