package com.cyyaw.application.parking.gate.mqtt.broker.filter;


import com.cyyaw.application.parking.gate.common.GateConstants;
import com.cyyaw.application.parking.gate.common.JsonUtil;
import com.cyyaw.application.parking.gate.common.entity.mqtt.MqttPayload;
import com.cyyaw.application.parking.gate.common.entity.mqtt.OnlineDto;
import com.cyyaw.mqtt.client.MqttApplicationClient;
import com.cyyaw.netty.mqtt.common.entity.ValidateRest;
import com.cyyaw.netty.mqtt.common.filter.ConnectFilter;
import com.cyyaw.netty.mqtt.session.MqttSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 客户端验证
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ConnectFilterImpl implements ConnectFilter {


    private final MqttApplicationClient mqttClient;

    @Override
    public ValidateRest validateCredentials(String username, String password, String clientId) {
        log.info("MQTT 客户端连接验证，username:{},  clientId: {}", username, clientId);
        // 调用服务查询设备，clientId 是设备ID（设备 code）
//        AdminDeviceClient.ValidateResult vr = adminDeviceClient.validate(username, password, clientId);
//        ValidateRest validateRest = new ValidateRest();
//        validateRest.setAllowConnect(vr.allowConnect());
//        validateRest.setRole(vr.role());
//        if (Boolean.TRUE.equals(vr.allowConnect())) {
//            log.info("设备 {} 校验通过", clientId);
//        } else {
//            log.warn("设备 {} 校验失败，拒绝连接", clientId);
//        }

        ValidateRest validateRest = new ValidateRest();
        validateRest.setRole("admin");
        validateRest.setAllowConnect(true);
        return validateRest;
    }

    @Override
    public void handle(MqttSessionManager sessionManager, String clientId) {
        log.info("MQTT 客户端连接成功处理，clientId: {}", clientId);
        // 修改数据库设备状态：向云平台上报设备上线（对齐 MQTT停车场与云平台通信.md「上报 设备上线/下线」：
        // 主题 /server/parking/${设备编码}/thing/event/online/post，物模型事件 thing.event.online.post）
        String topic = "/server/parking/" + clientId + "/thing/event/online/post";
        String payload = buildOnlinePayload(clientId);
        if (payload == null) {
            log.warn("设备上线 MQTT 报文序列化失败，跳过发布：deviceCode={}", clientId);
        } else {
            mqttClient.publish(topic, payload);
        }
        mqttClient.subscribe("/device/parking/" + clientId + "/#");

    }

    /**
     * 构建设备上线/下线上报报文（thing.event.online.post）。
     */
    private String buildOnlinePayload(String deviceCode) {
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
        onlineDto.setOnline(true);
        payload.setParams(onlineDto);
        return JsonUtil.toJson(payload);
    }


}
