package com.cyyaw.application.parking.gate.mqtt;

import com.cyyaw.mqtt.client.MqttApplicationClient;
import com.cyyaw.mqtt.client.MqttCallBack;
import com.cyyaw.mqtt.client.entity.MqttObject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MqttClientConfig {

    @Autowired
    private CyawConfig cyawConfig;

    @Bean
    public MqttApplicationClient mqttClient() {
        MqttObject mqtt = cyawConfig.getMqtt();
        MqttApplicationClient mqttClient = new MqttApplicationClient();
        mqttClient.setMqttObject(mqtt);
        mqttClient.setMqttCallBack(new MqttCallBack() {

            @Override
            public void connect() {
                log.info("connect");
            }

            @Override
            public void connectFail() {
                log.error(" MQTT connectFail   ");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) {
                log.info("messageArrived");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                log.info("deliveryComplete");
            }

            @Override
            public void subscribeSuccess(String topic) {
                log.info("subscribeSuccess");
            }
        });
        mqttClient.initMqtt();
        return mqttClient;
    }


}

