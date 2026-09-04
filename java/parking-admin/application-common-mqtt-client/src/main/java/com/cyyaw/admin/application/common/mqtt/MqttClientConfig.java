package com.cyyaw.admin.application.common.mqtt;

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


    /**
     * MQTT 状态 ，
     */
    private static boolean mqttStatus = false;

    /**
     * 获取 MQTT 连接状态，供状态接口读取。
     * 由 MQTT 回调 connect()/connectFail() 实时更新。
     */
    public static boolean isMqttStatus() {
        return mqttStatus;
    }

    @Bean
    public MqttApplicationClient mqttClient() {
        MqttObject mqtt = cyawConfig.getMqtt();
        MqttApplicationClient mqttClient = new MqttApplicationClient();
        mqttClient.setMqttObject(mqtt);
        mqttClient.setMqttCallBack(new MqttCallBack() {

            @Override
            public void connect(boolean reconnect, String serverURI) {
                log.info("connect");
                mqttStatus = true;
            }

            @Override
            public void connectFail(Throwable cause) {
                log.error(" MQTT connectFail   ");
                mqttStatus = false;
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

