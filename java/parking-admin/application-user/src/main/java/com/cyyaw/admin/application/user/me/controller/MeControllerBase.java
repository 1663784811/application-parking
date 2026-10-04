package com.cyyaw.admin.application.user.me.controller;

import com.cyyaw.admin.application.user.service.AuUserService;
import com.cyyaw.admin.entity.em.SystemRoleEnum;
import com.cyyaw.admin.entity.module.user.AuUser;
import com.cyyaw.admin.entity.utils.LoginInfo;
import com.cyyaw.admin.entity.utils.LoginInfoContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * H5「我的」页面各控制器的公共部分：登录态校验 + 手机号补取。
 * <p>
 * 单独抽出来是因为 LoginInfo 里没有手机号，而「我的车辆/订单」要按
 * 「账号 ID 优先、手机号兜底」的维度归属，每个控制器都要先把手机号补出来。
 * 非 C 端角色（管理端）一律返回 null，由子类统一给「角色不对」。
 */
public abstract class MeControllerBase {

    @Autowired
    protected AuUserService auUserService;

    /** 只放行 C 端用户角色，其余返回 null */
    protected LoginInfo requireUser() {
        LoginInfo loginInfo = LoginInfoContext.getLoginInfo();
        if (loginInfo == null || !SystemRoleEnum.User.getRole().equals(loginInfo.getSystemRole())) {
            return null;
        }
        return loginInfo;
    }

    /** LoginInfo 里没有手机号，按用户 ID 回表取 */
    protected String phone(Long userId) {
        if (userId == null) {
            return null;
        }
        AuUser user = auUserService.findUserById(userId);
        return user == null ? null : user.getPhone();
    }
}
