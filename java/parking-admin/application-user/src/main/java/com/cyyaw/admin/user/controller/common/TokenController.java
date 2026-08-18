package com.cyyaw.admin.user.controller.common;

import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.user.LoginResult;
import com.cyyaw.admin.entity.dto.user.RefreshRequest;
import com.cyyaw.admin.entity.dto.user.UserInfoVo;
import com.cyyaw.admin.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * token：刷新（公开）+ 当前用户信息（需鉴权）。
 * <p>
 * /api/common/token/refreshToken 公开；/api/common/token/findUserInfo 受保护。
 */
@RestController
@RequestMapping("/api/common/token")
@RequiredArgsConstructor
public class TokenController {

    private final UserService userService;

    @PostMapping("/refreshToken")
    public BaseResult<LoginResult> refreshToken(@RequestBody RefreshRequest request) {
        LoginResult result = userService.refresh(request.getRefreshToken());
        return BaseResult.ok(result);
    }

    @GetMapping("/findUserInfo")
    public BaseResult<UserInfoVo> findUserInfo() {
        return BaseResult.ok(userService.findUserInfo());
    }

}
