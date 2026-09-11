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
public class ParkingEventHandle implements PublishMessageHandle {

    private final AdminCarNumberClient adminCarNumberClient;

    private final AdminDeviceClient adminDeviceClient;


    public String topic() {
        return "/server/parking/#";
    }

    @Override
    public void handle(MqttSessionManager sessionManager, String topic, byte[] payload) {
        // /server/parking/<deviceCode>/thing/event/<event>/post
        // topic 按斜杠分段：["", "server", "parking", "aaa", "thing", "event", "recognize", "post"]
        // 设备编码取第 4 段（下标 3），事件取第 7 段（下标 6）。
        String[] parts = topic.split("/");
        String deviceCode = parts.length > 3 ? parts[3] : "";

        // 处理
        log.info("接收消息处理: {} ,{}, deviceCode: {}", topic, new String(payload), deviceCode);

        // 车牌识别事件：转发给 admin 的 CarNumberController（/internal/parking/carNumber/recognize）
        if (parts.length > 6) {
            if ("recognize".equals(parts[6]) && !deviceCode.isEmpty()) {
                // 识别车牌
                adminCarNumberClient.forwardRecognize(deviceCode, payload);
            } else if ("online".equals(parts[6]) && !deviceCode.isEmpty()) {
                // 设备上报上线/下线：直接转发报文到 admin，由 admin 解析 params.online
                adminDeviceClient.updateOnlineStatus(deviceCode, payload);
            }
        }

    }
}
