package com.cyyaw.application.parking.gate.config;

import com.cyyaw.application.parking.gate.common.entity.GateUser;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 保安亭配置，对应 application.yml 中的 gate.* ：
 * <ul>
 *   <li>gate.jwt.secret / gate.jwt.expire —— JWT 签名与有效期</li>
 *   <li>gate.users —— 登录用户列表（无数据库）</li>
 *   <li>gate.plate —— 车牌识别（图片落地目录、MQTT 发布端 clientId）</li>
 * </ul>
 */
@Data
@ConfigurationProperties(prefix = "gate")
public class GateProperties {

    private Jwt jwt = new Jwt();

    /** 登录用户列表，账号密码写死在 application.yml */
    private List<GateUser> users = new ArrayList<>();

    /** 车牌识别相关配置（图片落地目录、MQTT 发布端 clientId） */
    private Plate plate = new Plate();

    @Data
    public static class Jwt {
        /** HS256 要求 >= 32 字节，生产环境务必修改 */
        private String secret = "cyyaw-parking-gate-jwt-secret-key-change-me-in-production-min-32-bytes";
        /** token 有效期（秒），默认 24 小时 */
        private long expire = 86400;
    }

    @Data
    public static class Plate {
        /** 车牌图片本地缓存目录（第一步下载落地） */
        private String imageDir = "./data/plate-img";

        /** MQTT 发布端 clientId（须与 cyyaw 订阅端 clientId 不同，否则互相挤掉） */
        private String publisherClientId = "parking-gate-publisher";
    }
}
