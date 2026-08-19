package com.cyyaw.admin.application.user.controller.login;

import com.cyyaw.admin.application.user.service.AdminLoginService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.user.login.LoginRequest;
import com.cyyaw.admin.entity.dto.user.login.RegisterEnterpriseRequest;
import com.cyyaw.admin.entity.dto.user.login.RegisterEnterpriseRest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "超级管理员登录模块")
@RequestMapping("/root/login")
public class RootLoginController {

    @Autowired
    public AdminLoginService adminLoginService;

    @Operation(summary = "超级管理员登录", description = "超级管理员登录")
    @PostMapping("/login")
    public BaseResult login(@RequestBody LoginRequest loginRequest) {
        return BaseResult.ok("登录成功");
    }


    @Operation(summary = "获取超级管理员登录信息", description = "获取超级管理员登录信息")
    @PostMapping("/findRootInfo")
    public BaseResult findRootInfo(String eCode) {
        return BaseResult.ok(adminLoginService.findAdminInfo(eCode));
    }


    @Operation(summary = "注册企业", description = "注册企业")
    @PostMapping("/registerEnterprise")
    public BaseResult<RegisterEnterpriseRest> registerEnterprise(@Valid @RequestBody RegisterEnterpriseRequest loginRequest) {
        RegisterEnterpriseRest rest = adminLoginService.registerEnterprise(loginRequest);
        return BaseResult.ok(rest, "注册企业成功");
    }

}
