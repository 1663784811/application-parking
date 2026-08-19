package com.cyyaw.admin.application.user.security;

/**
 * 以 ThreadLocal 持有当前请求的登录用户。
 * <p>
 * 由 {@link AuthFilter} 在鉴权通过后写入，请求结束后清理。
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    private UserContext() {
    }

}
