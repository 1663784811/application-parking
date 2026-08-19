package com.cyyaw.admin.application.user.controller.login;

import com.cyyaw.admin.application.user.service.StoreLoginService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.user.login.LoginRest;
import com.cyyaw.admin.entity.dto.user.login.StoreLoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "门店管理员登录")
@RestController
@RequestMapping("/store/login")
public class StoreLoginController {

    @Autowired
    private StoreLoginService storeLoginService;


    @Operation(summary = "门店管理员登录", description = "门店管理员登录")
    @PostMapping("/login")
    public BaseResult<LoginRest> login(@RequestBody StoreLoginRequest storeLoginRequest) {
        LoginRest rest = storeLoginService.login(storeLoginRequest);
        return BaseResult.ok(rest);
    }






}
