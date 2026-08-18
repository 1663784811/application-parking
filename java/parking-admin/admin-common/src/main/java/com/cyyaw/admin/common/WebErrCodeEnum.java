package com.cyyaw.admin.common;

/**
 * 页面错误码枚举
 */
public enum WebErrCodeEnum {
    /**
     * 成功
     */
    WEB_SUCCESS(2000, "操作成功"),
    /**
     * 失败
     */
    WEB_ERR(4000, "操作失败"),

    /**
     * 控制层
     */
    WEB_ILLEGALSTATE(5000, "参数错误"),

    /**
     * 登录相关
     */
    WEB_LOGINERR(6000, "登录失败"),
    WEB_NOT_LOGIN(6001, "该用户没有登录"),
    WEB_REGISTER_ERR(6002, "注册失败"),

    WEB_LOGIN_TIME_OUT(6010, "登录过期"),


    WEB_AUTHENTICATION_ERR(6020, "没有权限"),

    /**
     * 数据库操作查关
     */
    DATA_ERR(7000, "数据库错误"),
    DATA_ERR_MANY(7001, "数据库错误,可能存在多条相同的数据"),
    DATA_ERR_RELATION(7002, "数据库错误,请检SQL语句，或子表是否有数据"),
    DATA_ERR_NOTHING_WHERE(7003, "找不到查询条件");


    private Integer code;
    private String msg;

    WebErrCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
