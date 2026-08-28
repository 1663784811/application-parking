package com.cyyaw.application.parking.gate.auth;

import com.cyyaw.application.parking.gate.auth.model.GateUser;
import com.cyyaw.application.parking.gate.auth.model.LoginRequest;
import com.cyyaw.application.parking.gate.auth.model.LoginResult;
import com.cyyaw.application.parking.gate.auth.model.UserInfo;
import com.cyyaw.application.parking.gate.common.R;
import com.cyyaw.application.parking.gate.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 鉴权接口：
 * <ul>
 *   <li>POST /api/login  —— 账号密码登录，返回 token + 用户信息</li>
 *   <li>GET  /api/user/info —— 取当前登录用户信息（需带 token）</li>
 *   <li>POST /api/logout —— 登出（JWT 无状态，客户端清 token 即可）</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final GateUserStore userStore;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public R<LoginResult> login(@RequestBody LoginRequest req) {
        if (req == null || req.getUsername() == null || req.getPassword() == null) {
            return R.fail(400, "请输入账号和密码");
        }
        GateUser user = userStore.findByCredentials(req.getUsername(), req.getPassword());
        if (user == null) {
            return R.fail(401, "账号或密码错误");
        }
        String token = jwtUtil.generate(user);
        UserInfo userInfo = userStore.toUserInfo(user);
        return R.ok(new LoginResult(token, userInfo));
    }

    @GetMapping("/user/info")
    public R<UserInfo> userInfo(@RequestAttribute("currentUser") GateUser user) {
        return R.ok(userStore.toUserInfo(user));
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok();
    }
}
