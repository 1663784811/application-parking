package com.cyyaw.admin.application.common.mqtt;


import com.cyyaw.mqtt.client.MqttApplicationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mqtt 下发命令
 */
@Slf4j
@Component
public class IotService {

    @Autowired
    private MqttApplicationClient mqttApplicationClient;

    /**
     * 打开/关闭道闸
     */
    public void ctlBarrierGate(String code, boolean open) {




        log.info("============== 打开/关闭道闸 ==============");






        // mqttApplicationClient.publish("", "");








    }


}
