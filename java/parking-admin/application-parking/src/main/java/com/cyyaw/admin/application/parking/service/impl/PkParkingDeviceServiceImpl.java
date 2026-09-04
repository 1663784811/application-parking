package com.cyyaw.admin.application.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyyaw.admin.application.parking.service.PkParkingDeviceService;
import com.cyyaw.admin.dao.iot.IotDeviceDao;
import com.cyyaw.admin.dao.parking.PkChannelDao;
import com.cyyaw.admin.dao.parking.PkParkingDeviceDao;
import com.cyyaw.admin.entity.module.iot.IotDevice;
import com.cyyaw.admin.entity.module.parking.PkChannel;
import com.cyyaw.admin.entity.module.parking.PkParkingDevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PkParkingDeviceServiceImpl implements PkParkingDeviceService {

    @Autowired
    private PkParkingDeviceDao pkParkingDeviceDao;

    @Autowired
    private PkChannelDao pkChannelDao;

    @Autowired
    private IotDeviceDao iotDeviceDao;

    @Override
    public List<IotDevice> findDevicesByChannelId(Long channelId) {
        if (channelId == null) {
            return Collections.emptyList();
        }
        // 取该通道的所有绑定记录 → 设备ID批量查设备明细
        List<PkParkingDevice> rows = pkParkingDeviceDao.selectList(
                new QueryWrapper<PkParkingDevice>().eq("channel_id", channelId));
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> deviceIds = rows.stream().map(PkParkingDevice::getDeviceId).collect(Collectors.toList());
        List<IotDevice> devices = iotDeviceDao.selectBatchIds(deviceIds);
        // 按绑定记录的顺序输出
        Map<Long, IotDevice> byId = devices.stream()
                .collect(Collectors.toMap(IotDevice::getId, d -> d, (a, b) -> a, LinkedHashMap::new));
        List<IotDevice> ordered = new ArrayList<>();
        for (Long did : deviceIds) {
            IotDevice d = byId.get(did);
            if (d != null) {
                ordered.add(d);
            }
        }
        return ordered;
    }

    @Override
    public List<IotDevice> findUnboundDevices(Long parkingId) {
        // 全局已绑定的设备ID（设备全局唯一绑定一条通道，故不限停车场）
        List<PkParkingDevice> bound = pkParkingDeviceDao.selectList(
                new QueryWrapper<PkParkingDevice>().select("device_id"));
        QueryWrapper<IotDevice> w = new QueryWrapper<>();
        if (parkingId != null) {
            w.eq("business_id", parkingId);
        }
        if (!bound.isEmpty()) {
            w.notIn("id", bound.stream().map(PkParkingDevice::getDeviceId).collect(Collectors.toList()));
        }
        w.orderByDesc("create_time");
        return iotDeviceDao.selectList(w);
    }

    @Override
    public Map<Long, Integer> countByChannel(List<Long> channelIds) {
        Map<Long, Integer> result = new HashMap<>();
        if (channelIds == null || channelIds.isEmpty()) {
            return result;
        }
        // channel_id 分组计数；SELECT 与 GROUP BY 逐字一致，满足 MySQL only_full_group_by
        QueryWrapper<PkParkingDevice> w = new QueryWrapper<>();
        w.select("channel_id, count(*) as cnt")
                .in("channel_id", channelIds)
                .groupBy("channel_id");
        for (Map<String, Object> m : pkParkingDeviceDao.selectMaps(w)) {
            Object id = m.get("channel_id");
            Object cnt = m.get("cnt");
            if (id != null && cnt != null) {
                result.put(Long.parseLong(String.valueOf(id)), Integer.parseInt(String.valueOf(cnt)));
            }
        }
        return result;
    }

    @Override
    public void bind(Long deviceId, Long channelId) {
        // 校验通道存在，并取 parkingId/appId 冗余进绑定记录
        PkChannel channel = pkChannelDao.selectById(channelId);
        if (channel == null) {
            throw new RuntimeException("通道不存在");
        }
        // 校验设备存在
        IotDevice device = iotDeviceDao.selectById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        // 设备全局唯一绑定：已绑到其他通道则报错（DB 层另有 unique 兜底）
        Long bound = pkParkingDeviceDao.selectCount(
                new QueryWrapper<PkParkingDevice>().eq("device_id", deviceId));
        if (bound != null && bound > 0) {
            throw new RuntimeException("该设备已绑定到其他通道，请先解绑");
        }
        // 同一通道重复绑定也拦截
        Long dup = pkParkingDeviceDao.selectCount(
                new QueryWrapper<PkParkingDevice>().eq("channel_id", channelId).eq("device_id", deviceId));
        if (dup != null && dup > 0) {
            throw new RuntimeException("该设备已绑定到此通道");
        }
        PkParkingDevice rec = new PkParkingDevice();
        rec.setAppId(channel.getAppId());
        rec.setParkingId(channel.getParkingId());
        rec.setChannelId(channelId);
        rec.setDeviceId(deviceId);
        pkParkingDeviceDao.insert(rec);
    }

    @Override
    public void unbind(Long deviceId, Long channelId) {
        // 独立绑定表：删除即解绑，无需 UpdateWrapper 清空字段（规避 NOT_NULL 坑）
        pkParkingDeviceDao.delete(new QueryWrapper<PkParkingDevice>()
                .eq("channel_id", channelId)
                .eq("device_id", deviceId));
    }

}
