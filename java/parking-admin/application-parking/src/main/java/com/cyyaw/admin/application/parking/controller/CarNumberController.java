package com.cyyaw.admin.application.parking.controller;


import com.cyyaw.admin.application.parking.service.CarNumberService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.iot.RecognizeDto;
import com.cyyaw.admin.entity.dto.iot.mqtt.MqttPayload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 车牌识别处理。
 * <p>
 * 挂在 /internal/parking/carNumber：由 MQTT broker（netty-application-mqtt）收到
 * /server/parking/{deviceCode}/thing/event/recognize/post 后转发调用。
 * /internal/** 在 admin SecurityConfig 放行，broker 无鉴权即可直达。
 */
@Tag(name = "车牌识别处理")
@RestController
@RequestMapping("/internal/parking/carNumber")
public class CarNumberController {

    @Autowired
    private CarNumberService carNumberService;

    @Operation(summary = "车牌识别处理", description = "按设备编码定位摄像头设备 → 判断出入方向 → 下发开闸")
    @PostMapping("/recognize")
    public BaseResult<Map<String, Object>> recognize(@RequestBody MqttPayload<RecognizeDto> mqttPayload) {
        RecognizeDto params = mqttPayload.getParams();
        if (params == null || params.getCarNumber() == null || params.getCarNumber().isBlank()) {
            return BaseResult.fail("车牌号不能为空");
        }
        if (params.getDeviceCode() == null || params.getDeviceCode().isBlank()) {
            return BaseResult.fail("设备编码不能为空");
        }
        return BaseResult.ok(carNumberService.recognize(params));
    }

}
