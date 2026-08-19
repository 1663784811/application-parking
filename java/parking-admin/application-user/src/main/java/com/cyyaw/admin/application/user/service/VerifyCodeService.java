package com.cyyaw.admin.application.user.service;


import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.user.login.VerifyPhoneRequest;

public interface VerifyCodeService {

    /**
     * 获取图片验证码
     */
    BaseResult<String> getVerifyCode(String fingerprint);

    /**
     * 获取手机验证码
     * @param verifyRequest
     * @return
     */
    BaseResult<String> getVerifyPhoneCode(VerifyPhoneRequest verifyRequest);

    /**
     * 验证验证码
     */
    boolean verifyCode(String key, String code);

    /**
     * 删除验证
     */
    void deleteVerifyCode(String key);

    String getCode(String keyCode);


}
