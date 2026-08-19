package com.cyyaw.admin.application.user.controller.login;

import com.cyyaw.admin.application.user.service.VerifyCodeService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.common.CaptchaGenerator;
import com.cyyaw.admin.entity.dto.user.login.VerifyPhoneRequest;
import com.cyyaw.admin.entity.dto.user.login.VerifyRequest;
import com.cyyaw.admin.entity.redis.RedisKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.io.IOException;


@RestController
@Tag(name = "验证码")
@RequestMapping("/common/verify")
public class VerifyCodeController {

    @Autowired
    private VerifyCodeService verifyCodeService;

    @PostMapping("/getVerifyCode")
    @Operation(summary = "获取图片验证码")
    public BaseResult<String> getVerifyCode(@Valid @RequestBody VerifyRequest verifyRequest) {
        String fingerprint = verifyRequest.getFingerprint();
        return verifyCodeService.getVerifyCode(fingerprint);
    }

    @GetMapping("/getVerifyImg/{keyCode}")
    @Operation(summary = "获取验证码图片")
    public void getVerifyImg(@PathVariable("keyCode") String keyCode, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = verifyCodeService.getCode(RedisKey.VERIFY_CODE + ":" + keyCode);
        // 生成验证码
        CaptchaGenerator.CaptchaResult captcha = CaptchaGenerator.generate(code);
        captcha.getImageBase64();
        // 输出图片到响应流
        ImageIO.write(captcha.getImage(), "png", response.getOutputStream());
    }

    @PostMapping("/getVerifyPhoneCode")
    @Operation(summary = "获取手机验证码")
    public BaseResult<String> getVerifyPhoneCode(@RequestBody VerifyPhoneRequest verifyRequest) {
        return verifyCodeService.getVerifyPhoneCode(verifyRequest);
    }

}
