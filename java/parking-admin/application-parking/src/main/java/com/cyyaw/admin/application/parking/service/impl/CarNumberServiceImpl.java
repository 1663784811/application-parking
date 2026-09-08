package com.cyyaw.admin.application.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyyaw.admin.application.common.mqtt.IotService;
import com.cyyaw.admin.application.parking.service.CarNumberService;
import com.cyyaw.admin.application.parking.service.PkCarLogService;
import com.cyyaw.admin.dao.parking.PkChannelDao;
import com.cyyaw.admin.dao.parking.PkParkingDeviceDao;
import com.cyyaw.admin.entity.dto.iot.RecognizeDto;
import com.cyyaw.admin.entity.module.iot.IotDevice;
import com.cyyaw.admin.entity.module.parking.PkCarLog;
import com.cyyaw.admin.entity.module.parking.PkChannel;
import com.cyyaw.admin.entity.module.parking.PkParkingDevice;
import com.cyyaw.admin.inf.InfIot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @Autowired
    private PkCarLogService pkCarLogService;


    @Override
    public Map<String, Object> recognize(RecognizeDto dto) {
        String deviceCode = dto.getDeviceCode();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deviceCode", deviceCode);
        result.put("carNumber", dto.getCarNumber());
        result.put("carType", dto.getCarType());

        // 1. 通过 deviceCode 查摄像头设备（code 唯一，传 false 防脏数据抛 TooManyResultsException）
        IotDevice device = infIot.findIotDeviceByCode(deviceCode);
        if (device == null) {
            log.warn("车牌识别：设备不存在 deviceCode={}", deviceCode);
            result.put("direction", "unknown");
            result.put("action", "reject");
            result.put("message", "设备不存在: " + deviceCode);
            return result;
        }
        // 雪花 ID 以字符串返回，规避 JS Long 精度丢失（无全局 Long→String 序列化器）
        result.put("deviceId", String.valueOf(device.getId()));
        result.put("deviceName", device.getName());

        // 2. 取设备-通道绑定，判断出入方向 + 停车场ID（一次查询复用）
        PkParkingDevice binding = findBinding(device);
        String direction = resolveDirection(device, binding);
        Long parkingId = binding != null ? binding.getParkingId() : null;
        result.put("direction", direction);
        result.put("parkingId", parkingId == null ? null : String.valueOf(parkingId));
        Long enId = device.getEnId();
        // 3. 按方向处理
        switch (direction) {
            case "in" -> handleEntry(enId, dto, binding, result);
            case "out" -> handleExit(enId, dto, binding, result);
            case "inout" -> {
                // 出入口：按是否已有在场记录判定出入（有在场记录→出场，否则→入场）
                if (parkingId == null) {
                    result.put("action", "reject");
                    result.put("message", "设备未绑定停车场，无法判定出入");
                } else {
                    PkCarLog openLog = pkCarLogService.selectByParkingIdAndCarNumber(parkingId, dto.getCarNumber());
                    if (openLog == null) {
                        handleEntry(enId, dto, binding, result);
                    } else {
                        handleExit(enId, dto, binding, result);
                    }
                }
            }
            default -> {
                result.put("action", "reject");
                result.put("message", "无法判断出入方向，不下发开闸");
            }
        }

        return result;
    }

    /**
     * 入场处理：创建在场停车记录（status=0），下发开闸。
     * <p>订单创建待订单服务就绪后接入（见 TODO）。
     */
    private void handleEntry(Long enId, RecognizeDto dto, PkParkingDevice binding, Map<String, Object> result) {
        Long parkingId = binding != null ? binding.getParkingId() : null;
        if (parkingId == null) {
            result.put("action", "reject");
            result.put("message", "设备未绑定停车场，无法登记入场");
            return;
        }
        PkCarLog carLog = new PkCarLog();
        carLog.setParkingId(parkingId);
        carLog.setAppId(binding.getAppId());
        carLog.setCarNumber(dto.getCarNumber());
        carLog.setCarType(dto.getCarType());
        carLog.setEntryTime(LocalDateTime.now());
        carLog.setStatus(0);
        carLog.setEnId(enId);
        PkCarLog saved = pkCarLogService.save(carLog);
        // TODO: 订单服务就绪后，在此创建入场订单（按入场时长计费）
        // 下发开闸（code 即识别设备编码；下游 publish 协议见 IotService.ctlBarrierGate）
        iotService.ctlBarrierGate(dto.getDeviceCode(), true);
        result.put("action", "entry");
        result.put("message", "入场放行");
        result.put("carLogId", saved == null || saved.getId() == null ? null : String.valueOf(saved.getId()));
    }

    /**
     * 出场处理：核对在场记录 → 结算出场（置 out_time、status=1）→ 下发开闸。
     * <p>无在场记录则拒绝放行；计费核销待订单服务就绪后接入（见 TODO）。
     */
    private void handleExit(Long enId, RecognizeDto dto, PkParkingDevice binding, Map<String, Object> result) {
        Long parkingId = binding != null ? binding.getParkingId() : null;
        if (parkingId == null) {
            result.put("action", "reject");
            result.put("message", "设备未绑定停车场，无法核对出场记录");
            return;
        }
        PkCarLog openLog = pkCarLogService.selectByParkingIdAndCarNumber(parkingId, dto.getCarNumber());
        if (openLog == null) {
            log.warn("车牌识别：出场但无入场记录 parkingId={}, carNumber={}", parkingId, dto.getCarNumber());
            result.put("action", "reject");
            result.put("message", "未找到入场记录，暂不放行");
            return;
        }
        // 结算出场：置出场时间与状态（status: 0=场内 → 1=已出场）
        openLog.setOutTime(LocalDateTime.now());
        openLog.setStatus(1);
        openLog.setEnId(enId);
        pkCarLogService.save(openLog);
        // TODO: 订单服务就绪后，按 entry_time ~ out_time 计算停车费并核销订单
        // 下发开闸
        iotService.ctlBarrierGate(dto.getDeviceCode(), true);
        result.put("action", "exit");
        result.put("message", "出场放行");
        result.put("carLogId", String.valueOf(openLog.getId()));
    }

    /**
     * 取设备与通道的绑定关系（一台设备只归属一条通道）。
     */
    private PkParkingDevice findBinding(IotDevice device) {
        if (device == null || device.getId() == null) {
            return null;
        }
        return pkParkingDeviceDao.selectOne(new QueryWrapper<PkParkingDevice>().eq("device_id", device.getId()), false);
    }

    /**
     * 判断出入方向：优先取设备所属通道的 type（in:入口 / out:出口 / inout:出入口），
     * 设备未绑定通道时回退到设备自身 location_type（1:入口, 2:出口）。
     * 两者都缺失则返回 unknown（调用方按不开闸处理）。
     */
    private String resolveDirection(IotDevice device, PkParkingDevice binding) {
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
