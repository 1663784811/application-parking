package com.cyyaw.admin.application.parking.service;

import com.cyyaw.admin.entity.dto.iot.RecognizeDto;

import java.util.Map;

public interface CarNumberService {

    /**
     * 车牌识别处理：按 deviceCode 定位摄像头设备 → 判断出入方向 → 下发开闸。
     * <p>返回决策摘要 {deviceCode, carNumber, direction, action, message}。
     */
    Map<String, Object> recognize(RecognizeDto dto);

}
