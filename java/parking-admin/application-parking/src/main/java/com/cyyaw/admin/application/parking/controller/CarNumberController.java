package com.cyyaw.admin.application.parking.controller;


import com.cyyaw.admin.application.parking.service.CarNumberService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.iot.RecognizeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "车牌识别处理")
@RestController
@RequestMapping("/parking/carnumber")
public class CarNumberController {

    @Autowired
    private CarNumberService carNumberService;

    @Operation(summary = "车牌识别处理", description = "按设备编码定位摄像头设备 → 判断出入方向 → 下发开闸")
    @PostMapping("/recognize")
    public BaseResult<Map<String, Object>> recognize(@RequestBody RecognizeDto recognizeDto) {
        if (recognizeDto == null || recognizeDto.getCarNumber() == null || recognizeDto.getCarNumber().isBlank()) {
            return BaseResult.fail("车牌号不能为空");
        }
        if (recognizeDto.getDeviceCode() == null || recognizeDto.getDeviceCode().isBlank()) {
            return BaseResult.fail("设备编码不能为空");
        }
        return BaseResult.ok(carNumberService.recognize(recognizeDto));
    }

}
