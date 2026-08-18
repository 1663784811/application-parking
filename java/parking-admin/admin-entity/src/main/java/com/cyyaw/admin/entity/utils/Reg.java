package com.cyyaw.admin.entity.utils;

public final class Reg {

    // 手机号
    public static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    // 密码 6-16位 数字或字母
    public static final String PASSWORD_REGEX = "^[a-zA-Z0-9]{6,16}$";
    // 用户名3至18位
    public static final String UserName_REGEX = "^[a-zA-Z0-9_]{3,18}$";
    // 指纹
    public static final String Fingerprint_REGEX = "^[a-zA-Z0-9_]{3,64}$";

    // 常规名称 2-18位长度
    public static final String NORMAL_NAME = "^.{2,18}$";


}
