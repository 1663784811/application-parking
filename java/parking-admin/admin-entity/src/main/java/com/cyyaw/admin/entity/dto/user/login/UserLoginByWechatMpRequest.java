package com.cyyaw.admin.entity.dto.user.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信公众号网页授权登录。昵称头像由微信直接返回，不需要前端上传。
 */
@Data
@Schema(description = "用户微信公众号网页授权登录")
public class UserLoginByWechatMpRequest implements Serializable {

    @Schema(description = "网页授权回调带回的code", example = "081Xxx...")
    private String code;

    @Schema(description = "本系统应用ID（au_app.id，不是微信的 appId）", example = "111")
    private Long appId;

    @Schema(description = "门店ID", example = "222")
    private Long storeId;

}
