package com.cyyaw.admin.entity.dto.user.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信小程序登录。code 是 wx.login 拿到的临时凭证，不是短信验证码，
 * 所以这里不继承 UserLogin（那条线带着 @NotBlank 的 fingerprint）。
 */
@Data
@Schema(description = "用户微信小程序登录")
public class UserLoginByWechatMaRequest implements Serializable {

    @Schema(description = "微信登录凭证", example = "081Xxx...")
    private String code;

    @Schema(description = "本系统应用ID（au_app.id，不是微信的 appId）", example = "111")
    private Long appId;

    @Schema(description = "门店ID", example = "222")
    private Long storeId;

    @Schema(description = "昵称", example = "微信用户")
    private String nickName;

    @Schema(description = "头像", example = "https://thirdwx.qlogo.cn/xxx")
    private String face;

}
