package com.cyyaw.application.parking.gate.mqtt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * MQTT 配置属性，对应 application.yml 的 cyyaw.mqtt.*。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cyaw.mqtt")
public class MqttProperties {

    /**
     * 是否启用 MQTT 客户端。false 时不创建连接，不影响应用启动。
     */
    private boolean enabled = false;

    /**
     * MQTT 服务器地址，如 tcp://127.0.0.1:1883
     */
    private List<String> tcp = new ArrayList<>();

    /**
     * 用户名
     */
    private String userName = "";

    /**
     * 密码
     */
    private String password = "";

    /**
     * 客户端 ID，留空则自动生成 parking-gate-<uuid>，保证唯一性
     */
    private String clientId = "";

    /**
     * 操作完成超时（毫秒），用于 Paho 执行器与入站适配器完成超时
     */
    private int timeOut = 10000;

    /**
     * 连接超时（秒），Paho 与 broker 完成 CONNECT 握手的最长等待时间。
     * 超过该时间未连上则判定连接失败（触发 MqttConnectionFailedEvent）。
     */
    private int connectTimeout = 30;

    /**
     * 默认 QoS：0 最多一次 / 1 至少一次 / 2 恰好一次
     */
    private int defaultQos = 1;

    /**
     * 订阅主题列表（入站），按需配置
     */
    private List<String> topics = new ArrayList<>();
}
