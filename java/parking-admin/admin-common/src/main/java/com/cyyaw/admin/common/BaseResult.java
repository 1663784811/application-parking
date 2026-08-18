package com.cyyaw.admin.common;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BaseResult<T> implements Serializable {

    /**
     * 分页信息
     */
    private Result result;
    /**
     * 主要数据 可能是Object 或  Arr
     */
    private T data;
    /**
     * 提示信息
     */
    private String msg;
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 状态返回
     */
    public static <T> BaseResult<T> rest(WebErrCodeEnum codeEnum) {
        return createResult(codeEnum, null, null, null);
    }

    /**
     * 状态返回
     */
    public static <T> BaseResult<T> rest(WebErrCodeEnum codeEnum, String msg) {
        return createResult(codeEnum, null, null, msg);
    }

    /**
     * 成功
     */
    public static <T> BaseResult<T> ok() {
        return createResult(WebErrCodeEnum.WEB_SUCCESS, null, null, null);
    }

    /**
     * 成功
     *
     * @param data 数据
     */
    public static <T> BaseResult<T> ok(T data) {
        return createResult(WebErrCodeEnum.WEB_SUCCESS, data, null, null);
    }

    /**
     * 成功
     *
     * @param data 数据
     * @param msg  提示
     * @return
     */
    public static <T> BaseResult<T> ok(T data, String msg) {
        return createResult(WebErrCodeEnum.WEB_SUCCESS, data, null, msg);
    }

    /**
     * 成功
     *
     * @param data   数据
     * @param result 分页信息
     * @return
     */
    public static <T> BaseResult<T> ok(T data, Result result) {
        return createResult(WebErrCodeEnum.WEB_SUCCESS, data, result, null);
    }

    public static <T> BaseResult<List<T>> ok(List<T> data, Result result) {
        return createResult(WebErrCodeEnum.WEB_SUCCESS, data, result, null);
    }

    /**
     * 成功
     *
     * @param data   数据
     * @param result 分页信息
     * @return
     */
    public static <T> BaseResult<T> ok(T data, Result result, String msg) {
        return createResult(WebErrCodeEnum.WEB_SUCCESS, data, result, msg);
    }


    /**
     * 失败
     */
    public static <T> BaseResult<T> fail() {
        return createResult(WebErrCodeEnum.WEB_ERR, null, null, null);
    }

    /**
     * 失败
     *
     * @param msg 信息
     */
    public static <T> BaseResult<T> fail(String msg) {
        return createResult(WebErrCodeEnum.WEB_ERR, null, null, msg);
    }


    private static <T> BaseResult<T> createResult(WebErrCodeEnum codeEnum, T data, Result result, String msg) {
        BaseResult<T> res = new BaseResult<T>();
        res.setCode(codeEnum.getCode());
        if (null != msg && !msg.isEmpty()) {
            res.setMsg(msg);
        } else {
            res.setMsg(codeEnum.getMsg());
        }
        res.setData(data);
        res.setResult(result);
        return res;
    }

    public static <T> BaseResult<T> fail(WebErrCodeEnum webErrCodeEnum) {
        return createResult(webErrCodeEnum, null, null, webErrCodeEnum.getMsg());
    }

    @Data
    public static class Result {
        private Long total;
        private Integer page;
        private Integer size;

        public Result() {
        }

        public Result(Integer page, Integer size, Long total) {
            this.total = total;
            this.page = page;
            this.size = size;
        }
    }
}