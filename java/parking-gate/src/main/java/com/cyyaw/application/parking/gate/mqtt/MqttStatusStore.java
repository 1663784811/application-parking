package com.cyyaw.application.parking.gate.mqtt;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MQTT 连接状态的全局存储（进程内单例）。
 * <p>供前端通过 {@code GET /api/mqtt/status} 查询，业务上视为“全局变量”：
 * 连接成功/断开/失败都会实时更新这里的状态。</p>
 * <ul>
 *   <li>connected    —— 是否已连接（volatile，可被任意线程读）</li>
 *   <li>lastChangeAt —— 最近一次状态变化时间</li>
 *   <li>lastError    —— 最近一次连接失败的简要原因</li>
 * </ul>
 * <p>注意：本组件不依赖 cyyaw.mqtt.enabled，始终存在。MQTT 未开启时保持
 * 默认 disconnected 状态，前端仍可查询到“未连接”。</p>
 */
@Component
public class MqttStatusStore {

    /** 是否已连接。 */
    private final AtomicBoolean connected = new AtomicBoolean(false);

    /** 最近一次状态变化的时间（毫秒时间戳）。 */
    private volatile long lastChangeAt = System.currentTimeMillis();

    /** 最近一次连接失败的简要原因（可空）。 */
    private volatile String lastError;

    /** 标记为已连接。 */
    public void markConnected() {
        connected.set(true);
        lastError = null;
        lastChangeAt = System.currentTimeMillis();
    }

    /** 标记为未连接（断线/失败），可选地携带失败原因。 */
    public void markDisconnected(String error) {
        connected.set(false);
        lastError = error;
        lastChangeAt = System.currentTimeMillis();
    }

    public boolean isConnected() {
        return connected.get();
    }

    /**
     * 组装前端可读的状态快照。
     * <p>用 LinkedHashMap 保证 key 顺序稳定；直接暴露给前端即可。</p>
     */
    public Map<String, Object> snapshot() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("connected", connected.get());
        map.put("status", connected.get() ? "connected" : "disconnected");
        map.put("lastChangeAt", lastChangeAt);
        map.put("lastChangeTime",
                LocalDateTime.ofInstant(Instant.ofEpochMilli(lastChangeAt), ZoneId.systemDefault()).toString());
        map.put("lastError", lastError);
        return map;
    }
}
