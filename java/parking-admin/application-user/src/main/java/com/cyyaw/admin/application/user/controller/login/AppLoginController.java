package com.cyyaw.admin.application.user.controller.login;


import com.cyyaw.admin.application.user.service.AppLoginService;
import com.cyyaw.admin.application.user.service.AuUserService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.user.login.*;
import com.cyyaw.admin.entity.module.user.AuUser;
import com.cyyaw.admin.entity.utils.LoginInfo;
import com.cyyaw.admin.entity.utils.LoginInfoContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "用户登录模块")
@RequestMapping("/app/login")
public class AppLoginController {

    @Autowired
    private AppLoginService appLoginService;

    @Autowired
    private AuUserService auUserService;


    @PostMapping("/login")
    @Operation(summary = "用户登录-用户名密码登录", description = "用户名密码登录")
    public BaseResult<LoginRest> login(@RequestBody UserLoginRequest loginRequest) {
        LoginRest login = appLoginService.login(loginRequest);
        return BaseResult.ok(login, "登录成功");
    }


    @PostMapping("/phoneLogin")
    @Operation(summary = "用户登录-手机验证码登录(或注册)", description = "手机验证码登录(或注册)")
    public BaseResult<LoginRest> phoneLogin(@RequestBody UserLoginByPhoneRequest phoneRequest) {
        LoginRest login = appLoginService.phoneLogin(phoneRequest);
        return BaseResult.ok(login, "登录成功");
    }

    @PostMapping("/register")
    @Operation(summary = "用户名密码注册", description = "用户名密码注册")
    public BaseResult<AuUser> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        AuUser register = appLoginService.register(userRegisterRequest);
        return BaseResult.ok(register, "注册成功");
    }

    @PostMapping("/forgetPassword")
    @Operation(summary = "忘记密码", description = "忘记密码")
    public BaseResult<Object> forgetPassword(@RequestBody UserForgetPasswordRequest forgetPasswordRequest) {
        AuUser auUser = appLoginService.forgetPwd(forgetPasswordRequest);
        if (auUser == null) return BaseResult.fail();
        return BaseResult.ok();
    }

    @PostMapping("/updatePassword")
    @Operation(summary = "修改密码", description = "修改密码")
    public BaseResult<Object> updatePassword(@RequestBody UserUpdatePasswordRequest updatePasswordRequest) {
        LoginInfo loginInfo = LoginInfoContext.getLoginInfo();
        AuUser auUser = appLoginService.updatePassword(loginInfo, updatePasswordRequest);
        if (auUser == null) return BaseResult.fail();
        return BaseResult.ok();
    }

    @PostMapping("/accountUse")
    @Operation(summary = "验证用户名是否可用", description = "验证用户名是否可用")
    public BaseResult<Boolean> accountUse(@RequestBody UserLoginRequest userLoginRequest) {
        String username = userLoginRequest.getUsername();
        Long appId = userLoginRequest.getAppId();
        AuUser auUser = auUserService.findUserByAccountAndAppId(username, appId);
        if (auUser == null) {
            return BaseResult.ok(true);
        }
        return BaseResult.ok(false);
    }
}
