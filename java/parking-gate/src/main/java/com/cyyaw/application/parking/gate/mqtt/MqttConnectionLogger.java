package com.cyyaw.application.parking.gate.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.integration.mqtt.event.MqttConnectionFailedEvent;
import org.springframework.stereotype.Component;

/**
 * MQTT 连接失败日志。
 * <p>连接成功日志由 {@link MqttConfig} 的 {@code ConnectCallback} 打印；
 * 连接失败（首次连接失败、连接被断开、自动重连期间持续失败）由
 * {@link Mqttv3ClientManager} 发布 {@link MqttConnectionFailedEvent}，
 * 本类监听该事件并打印 cause。</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "cyaw.mqtt", name = "enabled", havingValue = "true")
public class MqttConnectionLogger {

    private final MqttStatusStore mqttStatusStore;

    public MqttConnectionLogger(MqttStatusStore mqttStatusStore) {
        this.mqttStatusStore = mqttStatusStore;
    }

    @EventListener
    public void onConnectionFailed(MqttConnectionFailedEvent event) {
        Throwable cause = event.getCause();
        log.error("MQTT 连接失败, cause={}", cause == null ? "未知" : cause.getMessage());
        mqttStatusStore.markDisconnected(cause == null ? "连接失败" : cause.getMessage());
    }
}
