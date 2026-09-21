package com.cyyaw.admin.application.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 第三方登录配置。凭据按环境写在 single-all 的 application.yml 里（cyyaw.third-login）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "cyyaw.third-login")
public class ThirdLoginProperties {

    private Wechat wechat = new Wechat();

    private Alipay alipay = new Alipay();

    @Data
    public static class Wechat {

        /**
         * 小程序 appId
         */
        private String miniAppId;

        /**
         * 小程序 secret
         */
        private String miniSecret;

        /**
         * 公众号 appId（网页授权）
         */
        private String mpAppId;

        /**
         * 公众号 secret
         */
        private String mpSecret;

    }

    @Data
    public static class Alipay {

        private String appId;

        /**
         * 应用私钥（PKCS8，去掉头尾和换行）
         */
        private String privateKey;

        /**
         * 支付宝公钥
         */
        private String alipayPublicKey;

        /**
         * 网关地址
         */
        private String gateway = "https://openapi.alipay.com/gateway.do";

        /**
         * 授权页地址（只用来拼跳转链接，走的是 openauth 域名，与上面的 API 网关不是一回事；
         * 沙箱换成 https://openauth.alipaydev.com/oauth2/publicAppAuthorize.htm）
         */
        private String authUrl = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm";

        /**
         * 签名算法
         */
        private String signType = "RSA2";

    }

}
