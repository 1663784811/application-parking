package com.cyyaw.application.parking.gate.mqtt.broker.filter;

import com.cyyaw.netty.mqtt.common.filter.SubscribeFilter;
import com.cyyaw.netty.mqtt.session.MqttSessionManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 订阅主题
 */
@Slf4j
@AllArgsConstructor
@Component
public class SubscribeFilterImpl implements SubscribeFilter {


    @Override
    public boolean handle(MqttSessionManager sessionManager, String clientId, List<String> topic) {







        return false;
    }
}
