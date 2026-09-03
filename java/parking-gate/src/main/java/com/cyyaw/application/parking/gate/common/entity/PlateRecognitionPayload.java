package com.cyyaw.application.parking.gate.common.entity;

import lombok.Data;

/**
 * 车牌识别 MQTT 上报报文，对齐 MQTT停车场与云平台通信.md「上报 车牌识别」。
 * <p>主题：/server/parking/${设备编码}/thing/event/property/post
 * <pre>{@code
 * {
 *   "id": "10000",
 *   "version": "1.0",
 *   "sys": { "ack": 0, "deviceCode": "<边设备Code>", "childCode": null },
 *   "method": "thing.event.property.post",
 *   "params": { "deviceCode": "...", "carNumber": "...", "carType": "..." }
 * }
 * }</pre>
 * <p>字段名与文档 JSON 键逐字一致，Jackson 默认序列化即可，无需 @JsonProperty。
 * <p>边/子设备映射（单设备部署）：sys.deviceCode 与 params.deviceCode 同取识别相机编码，
 * childCode 暂置 null。若为「网关(边设备) 下挂多相机(子设备)」拓扑，应拆分——
 * sys.deviceCode 取边设备、sys.childCode/params.deviceCode 取相机。
 */
@Data
public class PlateRecognitionPayload {

    /** 报文 id（sys.ack=0 时不强制唯一，仅递增占位，对齐文档示例 "10002" 风格） */
    private String id;

    /** 版本，对齐文档 "1.0" */
    private String version = "1.0";

    /** 系统信息（ack/设备编码） */
    private Sys sys;

    /** 物模型事件 method：thing.event.property.post */
    private String method = "thing.event.property.post";

    /** 事件参数 */
    private Params params;

    /**
     * 系统信息块。
     */
    @Data
    public static class Sys {
        /** 应答模式：0=不需要平台回 ack */
        private int ack = 0;
        /** 边设备编码 */
        private String deviceCode;
        /** 子设备编码 */
        private String childCode;
    }

    /**
     * 事件参数块。
     */
    @Data
    public static class Params {
        /** 识别设备编码 */
        private String deviceCode;
        /** 车牌号 */
        private String carNumber;
        /** 车辆类型 */
        private String carType;
    }
}
