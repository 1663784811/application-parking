package com.cyyaw.netty.mqtt.message.parking;

import com.cyyaw.netty.mqtt.common.PublishMessageHandle;
import com.cyyaw.netty.mqtt.session.MqttSessionManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * iot 消息处理
 */
@Slf4j
@Component
@AllArgsConstructor
public class ParkingMessageHandle implements PublishMessageHandle {

    public String topic() {
        return "/server/parking/#";
    }

    @Override
    public void handle(MqttSessionManager sessionManager, String topic, byte[] payload) {

        // 处理
        log.info("接收消息处理: {} ,{}", topic, new String(payload));






    }
}
