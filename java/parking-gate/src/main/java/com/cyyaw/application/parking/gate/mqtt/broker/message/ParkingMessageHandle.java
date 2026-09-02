package com.cyyaw.application.parking.gate.mqtt.broker.message;

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












    }
}
