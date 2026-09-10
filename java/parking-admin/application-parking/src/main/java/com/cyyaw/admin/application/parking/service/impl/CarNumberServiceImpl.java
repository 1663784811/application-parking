package com.cyyaw.admin.application.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyyaw.admin.application.common.mqtt.IotService;
import com.cyyaw.admin.application.parking.service.CarNumberService;
import com.cyyaw.admin.application.parking.service.PkCarLogService;
import com.cyyaw.admin.dao.parking.PkCarLogDao;
import com.cyyaw.admin.dao.parking.PkChannelDao;
import com.cyyaw.admin.dao.parking.PkParkingDeviceDao;
import com.cyyaw.admin.entity.dto.iot.RecognizeDto;
import com.cyyaw.admin.entity.dto.iot.RecognizeVo;
import com.cyyaw.admin.entity.em.ChannelTypeEnum;
import com.cyyaw.admin.entity.em.IotDeviceTypeEnum;
import com.cyyaw.admin.entity.module.iot.IotDevice;
import com.cyyaw.admin.entity.module.parking.PkCarLog;
import com.cyyaw.admin.entity.module.parking.PkParkingDevice;
import com.cyyaw.admin.inf.InfIot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Autowired
    private PkCarLogDao pkCarLogDao;


    @Override
    public RecognizeVo recognize(RecognizeDto dto) {
        String deviceCode = dto.getDeviceCode();
        String carNumber = dto.getCarNumber();
        RecognizeVo vo = new RecognizeVo();
        vo.setDeviceCode(deviceCode);
        vo.setCarNumber(dto.getCarNumber());
        vo.setCarType(dto.getCarType());
        // 1. 通过 deviceCode 查摄像头设备（code 唯一，传 false 防脏数据抛 TooManyResultsException）
        IotDevice device = infIot.findIotDeviceByCode(deviceCode);
        if (device == null) {
            log.warn("车牌识别：设备不存在 deviceCode={}", deviceCode);
            vo.setDirection("unknown");
            vo.setAction("reject");
            vo.setMessage("设备不存在: " + deviceCode);
            return vo;
        }
        // 识别事件仅来自摄像头：非摄像头设备不下发处理（摄像头与通道一对一，方向只可能是 in/out）
        if (!IotDeviceTypeEnum.CAMERA.getType().equals(device.getType())) {
            log.warn("车牌识别：设备非摄像头，忽略 deviceCode={}, type={}", deviceCode, device.getType());
            vo.setDirection("unknown");
            vo.setAction("reject");
            vo.setMessage("识别设备必须是摄像头: " + deviceCode);
            return vo;
        }
        // 雪花 ID 用 Long 承载，序列化时 @JsonFormat(shape = STRING) 转字符串（规避 JS Long 精度丢失）
        vo.setDeviceId(device.getId());
        vo.setDeviceName(device.getName());
        // 2. 取设备-通道绑定，判断出入方向 + 停车场ID（一次查询复用）
        PkParkingDevice binding = pkParkingDeviceDao.selectOne(new QueryWrapper<PkParkingDevice>().eq("device_id", device.getId()), false);
        String code = device.getCode();
        Long parkingId = binding.getParkingId();
        String channelType = binding.getChannelType();
        if (ChannelTypeEnum.IN.getType().equals(channelType)) {
            // 入口
            // 查询该车牌在这个停车场的停车记录
            List<PkCarLog> pkCarLogList = pkCarLogDao.selectUnfinishedLog(parkingId, carNumber);
            if (pkCarLogList.size() > 0) {
                for (int i = 0; i < pkCarLogList.size(); i++) {
                    PkCarLog pkCarLog = pkCarLogList.get(i);
                    // TODO 结束订单、修改为已出场状态

                }
            }
            // 生成 日志记录、 新订单


            // 开闸
            iotService.ctlBarrierGate(code, true);
            // 显示屏
            iotService.ctlScreen(code, "欢迎 " + carNumber + "进场");

        } else if (ChannelTypeEnum.OUT.getType().equals(channelType)) {
            // 出口

            // 开闸
            iotService.ctlBarrierGate(code, true);
        }


        return vo;
    }


}
