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
     * <p>
     * channelType 规则（摄像头与通道一对一：单台摄像头要么入口要么出口）：
     * <ul>
     *   <li>摄像头(camera) + 出入口(inout)通道：必须由调用方传 in/out 二选一；</li>
     *   <li>摄像头(camera) + 入口/出口通道：忽略入参，取通道自身 type；</li>
     *   <li>道闸(gate)：忽略入参，强制取通道自身 type；</li>
     *   <li>其余类型：默认取通道自身 type。</li>
     * </ul>
     */
    void bind(Long deviceId, Long channelId, String channelType);

    /**
     * 解绑：删除该通道上该设备的绑定记录。
     */
    void unbind(Long deviceId, Long channelId);

}
