package com.cyyaw.netty.mqtt.ctl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ParkingMqtt {


    /**
     * 显示显示
     */
    public void showDisplay(String clientId, String msg) {
//        JSONObject json = new JSONObject();
//        String topic = "/parking/device/" + clientId + "/display";
//        json.set("msg", msg);
//        json.set("code", clientId);
//        publishHandle.publish(topic, json.toString());
    }

    /**
     * 开闸
     */
    public void openTheGate(String clientId) {
//        JSONObject json = new JSONObject();
//        String topic = "/parking/device/" + clientId + "/gate";
//        json.set("open", true);
//        json.set("code", clientId);
//        publishHandle.publish(topic, json.toString());
    }

}
