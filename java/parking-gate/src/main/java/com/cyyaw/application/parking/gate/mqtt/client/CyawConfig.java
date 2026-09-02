package com.cyyaw.application.parking.gate.mqtt.client;

import com.cyyaw.mqtt.client.entity.MqttObject;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "cyyaw")
public class CyawConfig {

    private MqttObject mqtt;

}
