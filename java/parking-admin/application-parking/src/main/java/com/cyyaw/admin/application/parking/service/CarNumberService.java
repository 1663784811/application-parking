package com.cyyaw.admin.application.parking.service;

import com.cyyaw.admin.entity.dto.iot.RecognizeDto;
import com.cyyaw.admin.entity.dto.iot.RecognizeVo;

public interface CarNumberService {

    /**
     * 车牌识别处理：按 deviceCode 定位摄像头设备 → 判断出入方向 → 下发开闸。
     * <p>返回 {@link RecognizeVo}（含 direction/action/message 及停车记录ID等）。
     */
    RecognizeVo recognize(RecognizeDto dto);

}
