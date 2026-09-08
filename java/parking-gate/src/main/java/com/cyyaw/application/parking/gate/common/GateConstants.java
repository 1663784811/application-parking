package com.cyyaw.application.parking.gate.common;

import tools.jackson.databind.ObjectMapper;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 停车场门口端（parking-gate）共享常量与单例工具。
 * <p>从 PlateRecognitionController 抽出，供 gate 模块各组件复用——
 * MQTT 主题/报文 id、JSON 序列化器、通行记录时间格式等。
 */
public final class GateConstants {

    private GateConstants() {
    }

    // ===== 时间格式 =====

    /** 通行记录时间显示格式（HH:mm:ss） */
    public static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ===== MQTT 平台协议（对齐 MQTT停车场与云平台通信.md） =====

    /**
     * MQTT 主题前缀，匹配 admin 侧 ParkingMessageHandle 订阅的 /server/parking/#
     */
    public static final String MQTT_TOPIC_PREFIX = "/server/parking/";

    /**
     * 主题后缀，对齐云平台物模型事件上报路径 thing/event/property/post
     * （见 MQTT停车场与云平台通信.md「上报 车牌识别」）。完整主题：
     * /server/parking/${设备编码}/thing/event/property/post
     */
    public static final String MQTT_TOPIC_SUFFIX = "/thing/event/property/post";

    public static final String MQTT_TOPIC_RECOGNIZE = "/thing/event/recognize/post";

    /**
     * 报文 id（sys.ack=0 时不强制唯一，仅递增占位，对齐文档示例 "10002" 风格）
     */
    public static final AtomicLong MSG_ID = new AtomicLong(10000);


}
