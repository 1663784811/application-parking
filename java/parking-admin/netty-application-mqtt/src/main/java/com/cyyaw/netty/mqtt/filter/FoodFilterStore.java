package com.cyyaw.netty.mqtt.filter;

import com.cyyaw.netty.mqtt.common.filter.PublishFilter;
import io.netty.channel.Channel;
import org.springframework.stereotype.Component;


@Component
public class FoodFilterStore implements PublishFilter {

    @Override
    public boolean handle(Channel channel, String topic, byte[] payload) {

        return true;
    }
}
