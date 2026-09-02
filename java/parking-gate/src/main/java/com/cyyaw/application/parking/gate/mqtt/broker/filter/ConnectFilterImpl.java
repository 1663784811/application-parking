package com.cyyaw.application.parking.gate.mqtt.broker.filter;


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
        // 修改数据库设备状态
    }


}
