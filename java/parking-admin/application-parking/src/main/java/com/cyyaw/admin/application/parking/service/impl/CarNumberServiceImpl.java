package com.cyyaw.admin.application.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyyaw.admin.application.common.mqtt.IotService;
import com.cyyaw.admin.application.parking.CostUtil;
import com.cyyaw.admin.application.parking.service.CarNumberService;
import com.cyyaw.admin.application.parking.service.ParkingExitService;
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
import java.math.RoundingMode;
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

    @Autowired
    private ParkingExitService parkingExitService;


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
            carOutParking(parkingId, carNumber, carType, device, code);
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
                // 旧记录对应的订单也要收尾：按实际时长结算金额并标注结束原因，
                // 否则这笔订单会永远挂在初始的 0 元上（关联链路见 ParkingExitService#settleStaleOrder）
                parkingExitService.settleStaleOrder(oldLog);
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
        OrOrder carNumberOrder = infOrder.createOrder(orderDetailList);
        if (null != carNumberOrder) {
            // 开闸
            iotService.ctlBarrierGate(code, true);
            // 显示屏
            iotService.ctlScreen(code, "欢迎 " + carNumber + "进场");
        }
    }

    /**
     * 出场识别。登记「该车正在本通道等待缴费出场」，按规则算出应缴金额：
     * 金额为 0 直接免费放行；金额大于 0 则不开闸，等车主扫码支付后由
     * ParkingExitService#completeExit 放行。
     */
    private void carOutParking(Long parkingId, String carNumber, String carType, IotDevice device, String code) {
        PkCarLog carLog = pkCarLogService.selectByParkingIdAndCarNumber(parkingId, carNumber);
        if (carLog == null) {
            // 没有入场记录（识别漏拍/未登记），无法计费，直接放行避免堵车
            log.warn("出场识别：停车场 {} 未找到车牌 {} 的在场记录，直接开闸", parkingId, carNumber);
            iotService.ctlBarrierGate(code, true);
            return;
        }
        // 登记待出场通道，H5 扫通道码时据此带出车牌
        carLog.setOutChannelId(channelIdOf(device));
        carLog.setOutDeviceCode(code);
        carLog.setOutRecognizeTime(LocalDateTime.now());
        pkCarLogService.save(carLog);

        OrOrder order = parkingExitService.refreshOrder(carLog);
        if (order == null) {
            log.warn("出场识别：停车记录 {} 没有关联订单，直接开闸", carLog.getId());
            iotService.ctlBarrierGate(code, true);
            return;
        }
        BigDecimal amount = order.getPayAmount() == null ? BigDecimal.ZERO : order.getPayAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            // 免费放行：金额为 0 视同已结清，结束记录并开闸
            parkingExitService.completeExit(order.getId(), null, null);
            return;
        }
        // 待缴费：不开闸，显示屏提示车主扫码支付
        iotService.ctlScreen(code, carNumber + " 停车" + CostUtil.formatDuration(carLog.getEntryTime(), LocalDateTime.now())
                + ",请支付" + amount.setScale(2, RoundingMode.HALF_UP) + "元");
    }

    /**
     * 取设备绑定的通道ID（出场记录要落到具体通道上）。
     */
    private Long channelIdOf(IotDevice device) {
        PkParkingDevice binding = pkParkingDeviceDao.selectOne(
                new QueryWrapper<PkParkingDevice>().eq("device_id", device.getId()), false);
        return binding == null ? null : binding.getChannelId();
    }

}
