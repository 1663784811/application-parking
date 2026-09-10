package com.cyyaw.netty.mqtt.message.parking;

import com.cyyaw.netty.mqtt.client.AdminCarNumberClient;
import com.cyyaw.netty.mqtt.client.AdminDeviceClient;
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
public class ParkingServiceHandle implements PublishMessageHandle {

    private final AdminCarNumberClient adminCarNumberClient;

    private final AdminDeviceClient adminDeviceClient;

    // /device/parking/${设备编码}/thing/service/{事件标识符}/set
    public String topic() {
        return "/device/parking/#";
    }

    @Override
    public void handle(MqttSessionManager sessionManager, String topic, byte[] payload) {

        log.info("下发给设备数据: {}  , {} ", topic, new String(payload));

        sessionManager.publish(topic, payload);
    }
}
