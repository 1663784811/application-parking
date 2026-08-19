package com.cyyaw.admin.application.user.controller.login;


import com.cyyaw.admin.application.user.service.AdminLoginService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.user.login.AdminRegisterRequest;
import com.cyyaw.admin.entity.dto.user.login.LoginRequest;
import com.cyyaw.admin.entity.dto.user.login.LoginRest;
import com.cyyaw.admin.entity.module.user.AuAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "后台登录模块")
@RestController
@RequestMapping("/admin/login")
public class AdminLoginController {

    @Autowired
    public AdminLoginService adminLoginService;

    @Operation(summary = "企业管理员登录", description = "企业管理员登录")
    @PostMapping("/login")
    public BaseResult<LoginRest> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginRest rest = adminLoginService.login(loginRequest);
        return BaseResult.ok(rest, "登录成功");
    }

    @Operation(summary = "企业管理员注册", description = "企业管理员注册")
    @PostMapping("/register")
    public BaseResult register(@RequestBody AdminRegisterRequest adminRegisterRequest) {
        AuAdmin admin = adminLoginService.register(adminRegisterRequest);
        return BaseResult.ok(admin, "注册成功");
    }

}
