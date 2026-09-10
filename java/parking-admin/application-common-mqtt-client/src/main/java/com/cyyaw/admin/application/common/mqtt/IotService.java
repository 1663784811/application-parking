package com.cyyaw.admin.application.common.mqtt;


import com.cyyaw.admin.common.json.JsonUtil;
import com.cyyaw.admin.entity.dto.iot.mqtt.MqttPayload;
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

        log.info("==============  打开/关闭道闸 ==============");

        MqttPayload<Object> mqttPayload = new MqttPayload<>();

        String msgJson = JsonUtil.beanToString(mqttPayload);
        // 道闸
        mqttApplicationClient.publish("/device/parking/" + code + "/thing/service/{事件标识符}/set", msgJson);
    }

    /**
     * 显示屏
     */
    public void ctlScreen(String code, String msg) {

        // 显示屏
        mqttApplicationClient.publish("/device/parking/" + code + "/thing/service/{事件标识符}/set", "");
    }
}
