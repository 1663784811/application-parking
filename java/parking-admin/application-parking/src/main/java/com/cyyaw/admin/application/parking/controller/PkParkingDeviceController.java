package com.cyyaw.admin.application.parking.controller;

import com.cyyaw.admin.application.parking.service.PkParkingDeviceService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.parking.PkParkingDeviceBindDTO;
import com.cyyaw.admin.entity.dto.parking.PkParkingDeviceCountByChannelDTO;
import com.cyyaw.admin.entity.dto.parking.PkParkingDeviceUnboundDTO;
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

    @Operation(summary = "未绑定通道的设备", description = "查询某停车场下未绑定通道的设备；parkingId 为空时查全部未绑设备；以 PkParkingDeviceUnboundDTO 接收查询参数")
    @GetMapping("/unbound")
    public BaseResult<List<IotDevice>> unbound(PkParkingDeviceUnboundDTO query) {
        return BaseResult.ok(pkParkingDeviceService.findUnboundDevices(query.getParkingId()));
    }

    @Operation(summary = "按通道统计设备数（批量）", description = "传 channelIds 集合，返回 { channelId: count }；以 PkParkingDeviceCountByChannelDTO 接收查询参数")
    @GetMapping("/countByChannel")
    public BaseResult<Map<Long, Integer>> countByChannel(PkParkingDeviceCountByChannelDTO query) {
        return BaseResult.ok(pkParkingDeviceService.countByChannel(query.getChannelIds()));
    }

    @Operation(summary = "绑定设备到通道", description = "设备全局唯一绑定。出入口通道的摄像头需传 channelType（in/out）；入口/出口通道及道闸由服务端按通道类型确定。deviceId/channelId 为路径参数，channelType 以 PkParkingDeviceBindDTO 接收")
    @PostMapping("/bind/{deviceId}/{channelId}")
    public BaseResult<Void> bind(@PathVariable Long deviceId, @PathVariable Long channelId, PkParkingDeviceBindDTO query) {
        pkParkingDeviceService.bind(deviceId, channelId, query.getChannelType());
        return BaseResult.ok();
    }

    @Operation(summary = "解绑设备", description = "删除该通道上该设备的绑定记录")
    @PostMapping("/unbind/{deviceId}/{channelId}")
    public BaseResult<Void> unbind(@PathVariable Long deviceId, @PathVariable Long channelId) {
        pkParkingDeviceService.unbind(deviceId, channelId);
        return BaseResult.ok();
    }

}
