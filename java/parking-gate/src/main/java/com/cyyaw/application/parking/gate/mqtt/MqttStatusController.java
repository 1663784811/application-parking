package com.cyyaw.application.parking.gate.mqtt;

import com.cyyaw.application.parking.gate.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * MQTT 状态查询接口。
 * <ul>
 *   <li>GET /api/mqtt/status —— 返回 MQTT 连接状态（需带登录 token）</li>
 * </ul>
 * <p>状态来自 {@link MqttStatusStore}，为进程内全局变量，由
 * {@link MqttConfig}（连接成功）和 {@link MqttConnectionLogger}（连接失败）实时更新。</p>
 */
@RestController
@RequestMapping("/api/mqtt")
@RequiredArgsConstructor
public class MqttStatusController {

    private final MqttStatusStore mqttStatusStore;

    @GetMapping("/status")
    public R<Map<String, Object>> status() {
        return R.ok(mqttStatusStore.snapshot());
    }
}
