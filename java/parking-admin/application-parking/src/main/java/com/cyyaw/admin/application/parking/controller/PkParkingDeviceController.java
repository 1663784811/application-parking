package com.cyyaw.admin.application.parking.controller;

import com.cyyaw.admin.application.parking.service.PkParkingDeviceService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.module.iot.IotDevice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "通道设备")
@RestController
@RequestMapping("/admin/parking/parkingDevice")
public class PkParkingDeviceController {

    @Autowired
    private PkParkingDeviceService pkParkingDeviceService;

    @Operation(summary = "通道已绑设备", description = "查询某通道已绑定的设备列表（含设备明细）")
    @GetMapping("/byChannel/{channelId}")
    public BaseResult<List<IotDevice>> byChannel(@PathVariable Long channelId) {
        return BaseResult.ok(pkParkingDeviceService.findDevicesByChannelId(channelId));
    }

    @Operation(summary = "未绑定通道的设备", description = "查询某停车场下未绑定通道的设备；parkingId 为空时查全部未绑设备")
    @GetMapping("/unbound")
    public BaseResult<List<IotDevice>> unbound(@RequestParam(required = false) Long parkingId) {
        return BaseResult.ok(pkParkingDeviceService.findUnboundDevices(parkingId));
    }

    @Operation(summary = "按通道统计设备数（批量）", description = "传 channelIds 逗号分隔，返回 { channelId: count }")
    @GetMapping("/countByChannel")
    public BaseResult<Map<Long, Integer>> countByChannel(@RequestParam List<Long> channelIds) {
        return BaseResult.ok(pkParkingDeviceService.countByChannel(channelIds));
    }

    @Operation(summary = "绑定设备到通道", description = "将指定设备绑定到指定通道（设备全局唯一绑定）")
    @PostMapping("/bind/{deviceId}/{channelId}")
    public BaseResult<Void> bind(@PathVariable Long deviceId, @PathVariable Long channelId) {
        pkParkingDeviceService.bind(deviceId, channelId);
        return BaseResult.ok();
    }

    @Operation(summary = "解绑设备", description = "删除该通道上该设备的绑定记录")
    @PostMapping("/unbind/{deviceId}/{channelId}")
    public BaseResult<Void> unbind(@PathVariable Long deviceId, @PathVariable Long channelId) {
        pkParkingDeviceService.unbind(deviceId, channelId);
        return BaseResult.ok();
    }

}
