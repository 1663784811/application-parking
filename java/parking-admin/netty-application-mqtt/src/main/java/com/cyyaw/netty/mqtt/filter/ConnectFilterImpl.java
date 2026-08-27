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

        ValidateRest validateRest = new ValidateRest();
        validateRest.setAllowConnect(false);
        return validateRest;
    }

    @Override
    public void handle(MqttSessionManager sessionManager, String clientId) {

    }


}
