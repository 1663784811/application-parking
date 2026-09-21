package com.cyyaw.admin.entity.em;

import lombok.Getter;

/**
 * 第三方登录平台
 */
@Getter
public enum ThirdPlatformEnum {

    WechatMa("wechat_ma"), WechatMp("wechat_mp"), Alipay("alipay");

    /**
     * 平台标识
     */
    private String platform;

    ThirdPlatformEnum(String platform) {
        this.platform = platform;
    }

}
