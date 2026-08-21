package com.cyyaw.admin.application.user.service.impl;

import com.cyyaw.admin.application.user.service.AuEnterpriseService;
import com.cyyaw.admin.application.user.service.VerifyCodeService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.common.WhyStringUtil;
import com.cyyaw.admin.entity.dto.user.login.VerifyPhoneRequest;
import com.cyyaw.admin.entity.redis.RedisKey;
import com.cyyaw.admin.entity.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VerifyCodeServiceImpl implements VerifyCodeService {


    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private AuEnterpriseService auEnterpriseService;


    @Override
    public BaseResult<String> getVerifyCode(String fingerprint) {
//        AuEnterprise auEnterprise = auEnterpriseService.findEnterpriseById(enId);
//        if (auEnterprise == null) {
//            WebException.fail(WebErrCodeEnum.WEB_ILLEGALSTATE);
//        }
        String code = WhyStringUtil.verifyCode(4);
        redisUtils.setString(RedisKey.VERIFY_CODE + ":" + fingerprint, code, RedisKey.VERIFY_CODE_EXPIRATION);
        return BaseResult.ok(fingerprint);
    }

    @Override
    public BaseResult<String> getVerifyPhoneCode(VerifyPhoneRequest verifyRequest) {
        String phone = verifyRequest.getPhone();
        String fingerprint = verifyRequest.getFingerprint();
        String code = WhyStringUtil.verifyCode(4);
        log.info("=======  验证码: {}", code);
        redisUtils.setString(RedisKey.phoneVerifyCodeKey(phone, fingerprint), code, RedisKey.VERIFY_CODE_EXPIRATION);
        // TODO 调用发送验证码接口

        return BaseResult.ok(fingerprint);
    }

    @Override
    public boolean verifyCode(String key, String code) {
        String s = redisUtils.getString(key);
        return code.equals(s);
    }

    @Override
    public void deleteVerifyCode(String key) {
        redisUtils.deleteKey(key);
    }

    @Override
    public String getCode(String keyCode) {
        // 从缓存中拿验证码
        return redisUtils.getString(keyCode);
    }
}
