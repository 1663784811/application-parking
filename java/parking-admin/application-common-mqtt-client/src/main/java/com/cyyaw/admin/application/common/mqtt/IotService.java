package com.cyyaw.admin.application.common.mqtt;


import cn.hutool.json.JSONObject;
import com.cyyaw.admin.entity.dto.iot.mqtt.GateControlDto;
import com.cyyaw.admin.entity.dto.iot.mqtt.MqttPayload;
import com.cyyaw.admin.entity.dto.iot.mqtt.ScreenControlDto;
import com.cyyaw.mqtt.client.MqttApplicationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Mqtt 下发命令
 * <p>
 * 对齐 MQTT停车场与云平台通信.md「下发」：主题
 * /device/parking/${设备编码}/thing/service/{事件标识符}/set，
 * 报文为 {@link MqttPayload} 信封（id/version/sys/method/params），
 * 与上行（online/recognize）报文结构一致。
 */
@Slf4j
@Component
public class IotService {

    /**
     * 报文 id（sys.ack=0 时不强制唯一，仅递增占位，对齐文档示例 "10002" 风格）
     */
    private static final AtomicLong MSG_ID = new AtomicLong(10000);

    @Autowired
    private MqttApplicationClient mqttApplicationClient;

    /**
     * 打开/关闭道闸
     * <p>主题 /device/parking/${code}/thing/service/gate/set，method thing.service.gate.set
     */
    public void ctlBarrierGate(String code, boolean open) {
        log.info("下发道闸指令: code={}, open={}", code, open);
        GateControlDto params = new GateControlDto();
        params.setDeviceCode(code);
        params.setOpen(open);
        publish("/device/parking/" + code + "/thing/service/gate/set", "thing.service.gate.set", code, params);
    }

    /**
     * 显示屏
     * <p>主题 /device/parking/${code}/thing/service/screen/set，method thing.service.screen.set
     */
    public void ctlScreen(String code, String msg) {
        log.info("下发显示屏指令: code={}, msg={}", code, msg);
        ScreenControlDto params = new ScreenControlDto();
        params.setDeviceCode(code);
        params.setMsg(msg);
        publish("/device/parking/" + code + "/thing/service/screen/set", "thing.service.screen.set", code, params);
    }

    /**
     * 构建下行物模型报文并发布。设备编码同时写入 sys.deviceCode 与 params.deviceCode，
     * 对齐上行报文（online/recognize）的 sys+params 双写约定。
     * <p>publish 失败仅记日志、不抛异常——下发为 fire-and-forget，瞬时 MQTT 故障
     * 不应阻断车牌识别主流程（订单已落库，开闸/显示屏失败不应让回调 500）。
     */
    private <T> void publish(String topic, String method, String deviceCode, T params) {
        MqttPayload<T> payload = new MqttPayload<>();
        payload.setId(String.valueOf(MSG_ID.getAndIncrement()));
        payload.setVersion("1.0");
        MqttPayload.Sys sys = new MqttPayload.Sys();
        sys.setAck(0);
        sys.setDeviceCode(deviceCode);
        sys.setChildCode("");
        payload.setSys(sys);
        payload.setMethod(method);
        payload.setParams(params);
        // 本模块不依赖 admin-common，直接用 hutool 序列化（与 JsonUtil.beanToString 同源：
        // new JSONObject(bean).toString()），保证下行报文与上行（online/recognize）序列化一致
        String json = new JSONObject(payload).toString();
        try {
            mqttApplicationClient.publish(topic, json);
        } catch (Exception e) {
            log.warn("MQTT 下发失败: topic={}, payload={}, err={}", topic, json, e.getMessage());
        }
    }

}
