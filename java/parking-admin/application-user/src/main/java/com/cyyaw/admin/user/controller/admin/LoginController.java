package com.cyyaw.admin.user.controller.admin;

import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.user.LoginRequest;
import com.cyyaw.admin.entity.dto.user.LoginResult;
import com.cyyaw.admin.entity.dto.user.RegisterRequest;
import com.cyyaw.admin.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录 / 注册。
 * <p>
 * 全部位于 /api/admin/login/**，被 AuthFilter 跳过（公开）。
 */
@RestController
@RequestMapping("/api/admin/login")
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    /** 后台登录（带验证码，fingerprint 实际为 verifyKey） */
    @PostMapping("/storeAdminLogin")
    public BaseResult<LoginResult> storeAdminLogin(@RequestBody LoginRequest request) {
        LoginResult result = userService.storeAdminLogin(
                request.getUsername(),
                request.getPassword(),
                request.getCode(),
                request.getFingerprint());
        return BaseResult.ok(result);
    }

    /** 账号密码登录（无验证码） */
    @PostMapping("/login")
    public BaseResult<LoginResult> login(@RequestBody LoginRequest request) {
        LoginResult result = userService.login(request.getUsername(), request.getPassword());
        return BaseResult.ok(result);
    }

    /** 注册 */
    @PostMapping("/register")
    public BaseResult<Void> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return BaseResult.ok();
    }

}
