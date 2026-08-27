package com.cyyaw.netty.mqtt.ctl.enity;

import lombok.Data;

/**
 * 摄像头
 */
@Data
public class CarInfo {

    /**
     * 硬件编号
     */
    private String code;

    /**
     * 车牌号
     */
    private String carNumber;

    /**
     * 车辆类型
     */
    private String carType;

    /**
     * 图片
     */
    private String img;

    /**
     * 车牌图片
     */
    private String numberImg;
}
