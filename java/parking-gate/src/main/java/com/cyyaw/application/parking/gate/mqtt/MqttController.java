package com.cyyaw.application.parking.gate.mqtt;

import com.cyyaw.application.parking.gate.common.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * MQTT 连接状态接口。
 * <ul>
 *   <li>GET /api/mqtt/status —— 返回门口端 MQTT 客户端连接状态（需带登录 token）</li>
 * </ul>
 * <p>状态来自 {@link MqttClientConfig#getMqttStatus()}，由 MQTT 回调实时更新；
 * 前端 Header 组件按固定间隔轮询该接口以展示连接状态。</p>
 */
@RestController
@RequestMapping("/api/mqtt")
public class MqttController {

    /**
     * 返回当前 MQTT 连接状态。
     * <ul>
     *   <li>connected：是否已连接</li>
     *   <li>lastError：最近一次断开原因（当前回调未提供错误详情，留空）</li>
     * </ul>
     */
    @GetMapping("/status")
    public R<Map<String, Object>> status() {
        return R.ok(Map.of(
                "connected", MqttClientConfig.isMqttStatus(),
                "lastError", ""
        ));
    }
}
