package com.cyyaw.netty.mqtt.filter;


import com.cyyaw.netty.mqtt.common.entity.ValidateRest;
import com.cyyaw.netty.mqtt.common.filter.ConnectFilter;
import com.cyyaw.netty.mqtt.session.MqttSessionManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 客户端验证
 */
@Slf4j
@AllArgsConstructor
@Component
public class ConnectFilterImpl implements ConnectFilter {

    @Override
    public ValidateRest validateCredentials(String username, String password, String clientId) {
        log.info("MQTT 客户端连接验证，username:{},  clientId: {}", username, clientId);
        // TODO 查数据库是否有这个设备

        ValidateRest validateRest = new ValidateRest();
        validateRest.setAllowConnect(true);
        validateRest.setRole("admin");
        return validateRest;
    }

    @Override
    public void handle(MqttSessionManager sessionManager, String clientId) {
        log.info("MQTT 客户端连接成功处理，clientId: {}", clientId);
        // 修改数据库设备状态

    }


}
