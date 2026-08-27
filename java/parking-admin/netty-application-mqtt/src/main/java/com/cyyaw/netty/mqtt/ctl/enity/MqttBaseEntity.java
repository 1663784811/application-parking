package com.cyyaw.netty.mqtt.ctl.enity;

import lombok.Data;

@Data
public class MqttBaseEntity<T> {

    /**
     * 硬件编号
     */
    private String code;

    /**
     * 参数
     */
    private T params;

    /**
     * 版本
     */
    private String version = "0.0.1";

    /**
     *
     */
    private String method;
}
