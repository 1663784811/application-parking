package com.cyyaw.application.parking.gate.common.entity.mqtt;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "MQTT 上行物模型报文")
public class MqttPayload<T> {

    @Schema(description = "报文 id（sys.ack=0 时不强制唯一，仅递增占位，对齐文档示例 \"10002\" 风格）", example = "10002")
    private String id;

    @Schema(description = "版本，对齐文档 \"1.0\"", example = "1.0")
    private String version;

    @Schema(description = "系统信息（ack/设备编码）")
    private Sys sys;

    @Schema(description = "物模型事件 method：thing.event.property.post", example = "thing.event.property.post")
    private String method = "";

    @Schema(description = "事件参数")
    private T params;

    /**
     * 系统信息块。
     */
    @Data
    @Schema(description = "系统信息块")
    public static class Sys {
        @Schema(description = "应答模式：0=不需要平台回 ack", example = "0")
        private int ack = 0;
        @Schema(description = "边设备编码")
        private String deviceCode;
        @Schema(description = "子设备编码")
        private String childCode;
    }
}
