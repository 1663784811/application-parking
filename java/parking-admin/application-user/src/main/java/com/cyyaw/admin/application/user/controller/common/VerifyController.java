package com.cyyaw.admin.application.user.controller.common;

import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.user.VerifyCodeRequest;
import com.cyyaw.admin.application.user.verify.CaptchaImageUtil;
import com.cyyaw.admin.application.user.verify.VerifyCodeStore;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 验证码：获取 verifyKey + 获取验证码图片。
 * <p>
 * 全部位于 /api/common/verify/**，被 AuthFilter 跳过（公开）。
 */
@Slf4j
@RestController
@RequestMapping("/api/common/verify")
@RequiredArgsConstructor
public class VerifyController {

    private final VerifyCodeStore verifyCodeStore;

    /**
     * 生成验证码，返回 verifyKey 字符串。
     * data 即 verifyKey，前端后续登录时将其作为 fingerprint 提交。
     */
    @PostMapping("/getVerifyCode")
    public BaseResult<String> getVerifyCode(@RequestBody(required = false) VerifyCodeRequest request) {
        String text = CaptchaImageUtil.randomText();
        String fingerprint = request != null ? request.getFingerprint() : null;
        String verifyKey = verifyCodeStore.put(text, fingerprint);
        return BaseResult.ok(verifyKey);
    }

    /**
     * 按 verifyKey 返回验证码图片 PNG。
     */
    @GetMapping("/getVerifyImg/{verifyKey}")
    public void getVerifyImg(@PathVariable String verifyKey,
                            HttpServletResponse response) throws IOException {
        VerifyCodeStore.Entry entry = verifyCodeStore.get(verifyKey);
        if (entry == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        byte[] png = CaptchaImageUtil.generate(entry.getText());
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        response.setContentLength(png.length);
        response.getOutputStream().write(png);
        response.getOutputStream().flush();
    }

}
