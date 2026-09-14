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
import com.cyyaw.admin.entity.module.or.OrOrder;
import com.cyyaw.admin.entity.module.or.OrOrderDetail;
import com.cyyaw.admin.entity.module.parking.PkCarLog;
import com.cyyaw.admin.entity.module.parking.PkParkingDevice;
import com.cyyaw.admin.inf.InfIot;
import com.cyyaw.admin.inf.InfOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Autowired
    private InfOrder infOrder;


    @Override
    public RecognizeVo recognize(RecognizeDto dto) {
        String deviceCode = dto.getDeviceCode();
        String carNumber = dto.getCarNumber();
        String carType = dto.getCarType();
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
            carInParking(parkingId, carNumber, carType, device, code);
        } else if (ChannelTypeEnum.OUT.getType().equals(channelType)) {
            // 出口
        }


        return vo;
    }

    private void carInParking(Long parkingId, String carNumber, String carType, IotDevice device, String code) {
        // 入口
        // 查询该车牌在这个停车场的停车记录
        List<PkCarLog> pkCarLogList = pkCarLogDao.selectUnfinishedLog(parkingId, carNumber);
        if (!pkCarLogList.isEmpty()) {
            // 该车牌在本停车场仍有未出场记录（上次未识别出场或异常重复入场），
            // 强制结束上一笔记录，避免脏数据残留。
            log.warn("车牌 {} 在停车场 {} 存在未出场记录，强制结束旧记录共 {} 笔", carNumber, parkingId, pkCarLogList.size());
            for (PkCarLog oldLog : pkCarLogList) {
                oldLog.setStatus(1);              // 1=已出场
                oldLog.setOutTime(LocalDateTime.now());
                pkCarLogService.save(oldLog);
                // 结束订单：当前停车流程尚未接入订单（application-order），且 PkCarLog 与 OrOrder
                // 之间无关联字段，待订单流建立 car_log_id 关联并在 application-parking 暴露接口后在此关闭。
                // 查询订单
            }
        }
        // 生成 日志记录、
        PkCarLog newLog = new PkCarLog();
        newLog.setParkingId(parkingId);
        newLog.setCarNumber(carNumber);
        newLog.setEntryTime(LocalDateTime.now());
        newLog.setStatus(0);
        newLog.setCarType(carType);
        newLog.setEnId(device.getEnId());
        newLog.setCreateTime(LocalDateTime.now());
        newLog.setUpdateTime(LocalDateTime.now());
        newLog.setNote("");
        PkCarLog pkCarLog = pkCarLogService.save(newLog);
        // 新订单
        List<OrOrderDetail> orderDetailList = new ArrayList<>();
        OrOrderDetail orderDetail = new OrOrderDetail();
        orderDetail.setBusinessId(pkCarLog.getId());
        orderDetail.setProductName("停车场: ,车牌:" + carNumber);
        orderDetail.setProductImage("");
        orderDetail.setProductPrice(new BigDecimal("0"));
        orderDetail.setQuantity(1);
        orderDetail.setTotalAmount(new BigDecimal("0"));
        orderDetail.setDiscountAmount(new BigDecimal("0"));
        orderDetail.setEnId(pkCarLog.getEnId());
        orderDetail.setCreateTime(LocalDateTime.now());
        orderDetail.setUpdateTime(LocalDateTime.now());
        orderDetail.setNote("");
        orderDetailList.add(orderDetail);
        OrOrder carNumberOrder = infOrder.createCarNumberOrder(orderDetailList);
        if (null != carNumberOrder) {
            // 开闸
            iotService.ctlBarrierGate(code, true);
            // 显示屏
            iotService.ctlScreen(code, "欢迎 " + carNumber + "进场");
        }
    }

    private void carOutParking(Long parkingId, String carNumber, String carType, IotDevice device, String code) {
        // 开闸
        iotService.ctlBarrierGate(code, true);

    }

}
