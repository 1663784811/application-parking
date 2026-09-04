package com.cyyaw.admin.application.parking.controller;


import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.iot.RecognizeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "车牌识别处理")
@RestController
@RequestMapping("/parking/carnumber")
public class CarNumberController {


    @Operation(summary = "车牌识别处理", description = "车牌识别处理")
    @PostMapping("/recognize")
    public BaseResult<Object> recognize(@RequestBody RecognizeDto recognizeDto) {
        String deviceCode = recognizeDto.getDeviceCode();
        String carNumber = recognizeDto.getCarNumber();
        String carType = recognizeDto.getCarType();

        // 通过deviceCode查摄像头设备，


        // 判断是出口还是入口


        //  开闸


        return BaseResult.ok();
    }


}
