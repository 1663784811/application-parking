package com.cyyaw.application.parking.gate.mqtt.broker.filter;


import com.cyyaw.application.parking.gate.common.GateConstants;
import com.cyyaw.application.parking.gate.common.JsonUtil;
import com.cyyaw.application.parking.gate.common.entity.mqtt.MqttPayload;
import com.cyyaw.application.parking.gate.common.entity.mqtt.OnlineDto;
import com.cyyaw.mqtt.client.MqttApplicationClient;
import com.cyyaw.netty.mqtt.common.filter.DisconnectFilter;
import com.cyyaw.netty.mqtt.session.MqttSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 关闭连接处理
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DisconnectFilterImpl implements DisconnectFilter {


    private final MqttApplicationClient mqttClient;

    @Override
    public void handle(MqttSessionManager sessionManager, String clientId) {
        log.info(" === 设备: {} ， 断开连接", clientId);
        // 向云平台上报设备下线（对齐 MQTT停车场与云平台通信.md「上报 设备上线/下线」：
        // 主题 /server/parking/${设备编码}/thing/event/online/post，物模型事件 thing.event.online.post）
        String topic = "/server/parking/" + clientId + "/thing/event/online/post";
        String payload = buildOnlinePayload(clientId, false);
        if (payload == null) {
            log.warn("设备下线 MQTT 报文序列化失败，跳过发布：deviceCode={}", clientId);
        } else {
            mqttClient.publish(topic, payload);
        }
    }

    /**
     * 构建设备上线/下线上报报文（thing.event.online.post）。
     */
    private String buildOnlinePayload(String deviceCode, boolean online) {
        MqttPayload<OnlineDto> payload = new MqttPayload<>();
        payload.setId(String.valueOf(GateConstants.MSG_ID.getAndIncrement()));
        payload.setVersion("1.0");
        MqttPayload.Sys sys = new MqttPayload.Sys();
        sys.setAck(0);
        sys.setDeviceCode(deviceCode);
        sys.setChildCode("");
        payload.setSys(sys);
        payload.setMethod("thing.event.online.post");
        OnlineDto onlineDto = new OnlineDto();
        onlineDto.setDeviceCode(deviceCode);
        onlineDto.setOnline(online);
        payload.setParams(onlineDto);
        return JsonUtil.toJson(payload);
    }


}
