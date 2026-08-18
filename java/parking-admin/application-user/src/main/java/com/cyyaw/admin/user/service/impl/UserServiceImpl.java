package com.cyyaw.admin.user.service.impl;

import com.cyyaw.admin.common.WebErrCodeEnum;
import com.cyyaw.admin.common.WebException;
import com.cyyaw.admin.entity.dto.user.LoginResult;
import com.cyyaw.admin.entity.dto.user.RegisterRequest;
import com.cyyaw.admin.entity.dto.user.UserInfoVo;
import com.cyyaw.admin.entity.module.user.User;
import com.cyyaw.admin.user.mapper.UserMapper;
import com.cyyaw.admin.user.security.JwtUtil;
import com.cyyaw.admin.user.security.LoginUser;
import com.cyyaw.admin.user.security.UserContext;
import com.cyyaw.admin.user.service.UserService;
import com.cyyaw.admin.user.verify.VerifyCodeStore;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 用户 / 登录 / token 业务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final VerifyCodeStore verifyCodeStore;

    @Override
    public LoginResult login(String username, String password) {
        User user = loadUser(username);
        checkPassword(password, user);
        return issue(user);
    }

    @Override
    public LoginResult storeAdminLogin(String username, String password, String code, String verifyKey) {
        if (!verifyCodeStore.validate(verifyKey, code)) {
            throw new WebException(WebErrCodeEnum.WEB_VERIFY_CODE_ERROR);
        }
        return login(username, password);
    }

    @Override
    public void register(RegisterRequest request) {
        if (userMapper.findByAccount(request.getUsername()) != null) {
            throw new WebException(WebErrCodeEnum.WEB_USER_EXISTS);
        }
        User user = new User();
        user.setTid(UUID.randomUUID().toString().replace("-", ""));
        user.setAccount(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public LoginResult refresh(String refreshToken) {
        Claims claims = jwtUtil.parse(refreshToken);
        String type = jwtUtil.getType(claims);
        if (!"refresh".equals(type)) {
            throw new WebException(WebErrCodeEnum.WEB_TOKEN_INVALID);
        }
        String tid = claims.getSubject();
        User user = userMapper.findByTid(tid);
        if (user == null) {
            throw new WebException(WebErrCodeEnum.WEB_USER_NOT_FOUND);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new WebException(WebErrCodeEnum.WEB_USER_DISABLED);
        }
        // 轮转：签发新的 access + refresh
        return issue(user);
    }

    @Override
    public UserInfoVo findUserInfo() {
        LoginUser loginUser = UserContext.get();
        if (loginUser == null) {
            throw new WebException(WebErrCodeEnum.WEB_TOKEN_INVALID);
        }
        User user = userMapper.findByTid(loginUser.getTid());
        if (user == null) {
            throw new WebException(WebErrCodeEnum.WEB_USER_NOT_FOUND);
        }
        UserInfoVo vo = new UserInfoVo();
        vo.setTid(user.getTid());
        vo.setAccount(user.getAccount());
        vo.setName(user.getName());
        vo.setPhone(user.getPhone());
        return vo;
    }

    private User loadUser(String username) {
        User user = userMapper.findByAccount(username);
        if (user == null) {
            throw new WebException(WebErrCodeEnum.WEB_USER_NOT_FOUND);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new WebException(WebErrCodeEnum.WEB_USER_DISABLED);
        }
        return user;
    }

    private void checkPassword(String rawPassword, User user) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new WebException(WebErrCodeEnum.WEB_PASSWORD_ERROR);
        }
    }

    private LoginResult issue(User user) {
        String access = jwtUtil.createAccessToken(user.getTid(), user.getAccount(), user.getName());
        String refresh = jwtUtil.createRefreshToken(user.getTid());
        return new LoginResult(access, refresh);
    }

}
