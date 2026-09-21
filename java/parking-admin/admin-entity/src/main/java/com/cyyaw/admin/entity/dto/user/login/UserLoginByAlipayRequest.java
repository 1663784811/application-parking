package com.cyyaw.admin.entity.dto.user.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户支付宝登录")
public class UserLoginByAlipayRequest implements Serializable {

    @Schema(description = "支付宝授权code", example = "081Xxx...")
    private String code;

    @Schema(description = "本系统应用ID（au_app.id，不是支付宝的 appId）", example = "111")
    private Long appId;

    @Schema(description = "门店ID", example = "222")
    private Long storeId;

    @Schema(description = "昵称", example = "支付宝用户")
    private String nickName;

    @Schema(description = "头像", example = "https://tfs.alipayobjects.com/xxx")
    private String face;

}
