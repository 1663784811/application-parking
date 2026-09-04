package com.cyyaw.admin.application.parking.service;

import com.cyyaw.admin.entity.module.iot.IotDevice;

import java.util.List;
import java.util.Map;

public interface PkParkingDeviceService {

    /**
     * 查询某通道已绑定的设备（含设备明细，按绑定顺序返回）
     */
    List<IotDevice> findDevicesByChannelId(Long channelId);

    /**
     * 查询某停车场下未绑定通道的设备（可绑定候选）；
     * parkingId 为空时查全部未绑设备。
     */
    List<IotDevice> findUnboundDevices(Long parkingId);

    /**
     * 批量统计各通道的已绑设备数 { channelId: count }
     */
    Map<Long, Integer> countByChannel(List<Long> channelIds);

    /**
     * 将设备绑定到通道（新增一条停车设备记录）。
     * 设备全局唯一绑定：已绑定到其他通道会抛异常。
     */
    void bind(Long deviceId, Long channelId);

    /**
     * 解绑：删除该通道上该设备的绑定记录。
     */
    void unbind(Long deviceId, Long channelId);

}
