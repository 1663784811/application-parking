package com.cyyaw.admin.entity.utils;

/**
 * 登录信息上下文
 */
public class LoginInfoContext {

    private static ThreadLocal<LoginInfo> loginInfo = new ThreadLocal<>();

    public static LoginInfo getLoginInfo() {
        return loginInfo.get();
    }

    public static void setLoginInfo(LoginInfo info) {
        loginInfo.set(info);
    }

    public static void clearLoginInfo() {
        loginInfo.remove();
    }

}
