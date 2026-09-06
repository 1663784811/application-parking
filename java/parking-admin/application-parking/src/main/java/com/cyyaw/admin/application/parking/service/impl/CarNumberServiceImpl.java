package com.cyyaw.admin.application.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyyaw.admin.application.common.mqtt.IotService;
import com.cyyaw.admin.application.parking.service.CarNumberService;
import com.cyyaw.admin.dao.iot.IotDeviceDao;
import com.cyyaw.admin.dao.parking.PkChannelDao;
import com.cyyaw.admin.dao.parking.PkParkingDeviceDao;
import com.cyyaw.admin.entity.dto.iot.RecognizeDto;
import com.cyyaw.admin.entity.module.iot.IotDevice;
import com.cyyaw.admin.entity.module.parking.PkChannel;
import com.cyyaw.admin.entity.module.parking.PkParkingDevice;
import com.cyyaw.admin.inf.InfIot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 车牌识别处理。
 */
@Slf4j
@Service
public class CarNumberServiceImpl implements CarNumberService {


    @Autowired
    private InfIot infIot;

    @Autowired
    private PkParkingDeviceDao pkParkingDeviceDao;

    @Autowired
    private PkChannelDao pkChannelDao;

    @Autowired
    private IotService iotService;


    @Override
    public Map<String, Object> recognize(RecognizeDto dto) {
        String deviceCode = dto.getDeviceCode();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deviceCode", dto.getDeviceCode());
        result.put("carNumber", dto.getCarNumber());
        result.put("carType", dto.getCarType());

        // 1. 通过 deviceCode 查摄像头设备（code 唯一，传 false 防脏数据抛 TooManyResultsException）
        IotDevice device = infIot.findIotDeviceByCode(deviceCode);;




        if (device == null) {
            log.warn("车牌识别：设备不存在 deviceCode={}", dto.getDeviceCode());
            result.put("direction", "unknown");
            result.put("action", "reject");
            result.put("message", "设备不存在: " + dto.getDeviceCode());
            return result;
        }
        // 雪花 ID 以字符串返回，规避 JS Long 精度丢失（无全局 Long→String 序列化器）
        result.put("deviceId", String.valueOf(device.getId()));
        result.put("deviceName", device.getName());

        // 2. 判断是出口还是入口
        String direction = resolveDirection(device);
        result.put("direction", direction);

        // 3. 开闸
        iotService.ctlBarrierGate("aaaaddd", true);


        return result;
    }

    /**
     * 判断出入方向：优先取设备所属通道的 type（in:入口 / out:出口 / inout:出入口），
     * 设备未绑定通道时回退到设备自身 location_type（1:入口, 2:出口）。
     * 两者都缺失则返回 unknown（调用方按不开闸处理）。
     */
    private String resolveDirection(IotDevice device) {
        PkParkingDevice binding = pkParkingDeviceDao.selectOne(new QueryWrapper<PkParkingDevice>().eq("device_id", device.getId()), false);
        if (binding != null && binding.getChannelId() != null) {
            PkChannel channel = pkChannelDao.selectById(binding.getChannelId());
            if (channel != null && channel.getType() != null && !channel.getType().isBlank()) {
                String t = channel.getType();
                return switch (t) {
                    case "in" -> "in";
                    case "out" -> "out";
                    case "inout" -> "inout";
                    default -> t;
                };
            }
        }
        Integer locationType = device.getLocationType();
        if (locationType != null) {
            return switch (locationType) {
                case 1 -> "in";
                case 2 -> "out";
                default -> "unknown";
            };
        }
        return "unknown";
    }


}
