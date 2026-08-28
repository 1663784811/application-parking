package com.cyyaw.application.parking.gate.mqtt;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * MQTT 发送工具，调用 {@link #sendMessage(String, String)} 向 broker 发布消息。
 * <p>QoS 取自 cyyaw.mqtt.defaultQos。</p>
 */
@Component
@AllArgsConstructor
@ConditionalOnProperty(prefix = "cyaw.mqtt", name = "enabled", havingValue = "true")
public class MqttTopic {

    private final MessageChannel mqttOutputChannel;

    private final MqttProperties mqttProperties;

    @Async
    public void sendMessage(String topic, String message) {
        int qos = mqttProperties.getDefaultQos();
        Message<String> mqttMessage = MessageBuilder.withPayload(message)
                .setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, qos)
                .build();
        mqttOutputChannel.send(mqttMessage);
    }
}
