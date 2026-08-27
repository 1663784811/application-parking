package com.cyyaw.netty.mqtt.filter;

import com.cyyaw.netty.mqtt.common.filter.DisconnectFilter;
import com.cyyaw.netty.mqtt.session.MqttSessionManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * 关闭连接处理
 */
@Slf4j
@AllArgsConstructor
@Component
public class DisconnectFilterImpl implements DisconnectFilter {


    @Override
    public void handle(MqttSessionManager sessionManager, String clientId) {
        log.info(" === 设备: {} ， 断开连接", clientId);
    }
}
