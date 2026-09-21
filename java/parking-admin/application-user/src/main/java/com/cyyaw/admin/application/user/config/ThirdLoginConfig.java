package com.cyyaw.admin.application.user.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第三方登录的 SDK 客户端。
 * <p>
 * 凭据没配置就不建 Bean，服务层用 ObjectProvider 注入、缺失时给出明确的“未配置”提示。
 * 这样密钥留空时应用照样能启动 —— DefaultAlipayClient 在构造阶段就会解析私钥，
 * 不拦一道会直接把启动带崩。
 * <p>
 * 判空用 {@code @ConditionalOnExpression} 而不是 {@code @ConditionalOnProperty}：
 * 后者只要 key 存在（哪怕值是空串）就会建 Bean，而 application.yml 里这些 key 是留空占位的。
 */
@Slf4j
@Configuration
public class ThirdLoginConfig {

    @Autowired
    private ThirdLoginProperties thirdLoginProperties;

    /**
     * 微信小程序
     */
    @Bean
    @ConditionalOnExpression("'${cyyaw.third-login.wechat.mini-app-id:}'.trim().length() > 0")
    public WxMaService wxMaService() {
        ThirdLoginProperties.Wechat wechat = thirdLoginProperties.getWechat();
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(wechat.getMiniAppId());
        config.setSecret(wechat.getMiniSecret());
        WxMaService wxMaService = new WxMaServiceImpl();
        wxMaService.setWxMaConfig(config);
        log.info("微信小程序登录已启用: appId={}", wechat.getMiniAppId());
        return wxMaService;
    }

    /**
     * 微信公众号（网页授权）
     */
    @Bean
    @ConditionalOnExpression("'${cyyaw.third-login.wechat.mp-app-id:}'.trim().length() > 0")
    public WxMpService wxMpService() {
        ThirdLoginProperties.Wechat wechat = thirdLoginProperties.getWechat();
        WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
        config.setAppId(wechat.getMpAppId());
        config.setSecret(wechat.getMpSecret());
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(config);
        log.info("微信公众号登录已启用: appId={}", wechat.getMpAppId());
        return wxMpService;
    }

    /**
     * 支付宝
     */
    @Bean
    @ConditionalOnExpression("'${cyyaw.third-login.alipay.app-id:}'.trim().length() > 0")
    public AlipayClient alipayClient() {
        ThirdLoginProperties.Alipay alipay = thirdLoginProperties.getAlipay();
        AlipayClient alipayClient = new DefaultAlipayClient(alipay.getGateway(), alipay.getAppId(),
                alipay.getPrivateKey(), "json", "UTF-8", alipay.getAlipayPublicKey(), alipay.getSignType());
        log.info("支付宝登录已启用: appId={}", alipay.getAppId());
        return alipayClient;
    }

}
