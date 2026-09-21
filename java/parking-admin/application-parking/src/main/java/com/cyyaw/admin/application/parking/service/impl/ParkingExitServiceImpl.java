package com.cyyaw.admin.application.parking.service.impl;

import com.cyyaw.admin.application.common.mqtt.IotService;
import com.cyyaw.admin.application.parking.CostUtil;
import com.cyyaw.admin.application.parking.service.PkCarLogService;
import com.cyyaw.admin.application.parking.service.PkChannelService;
import com.cyyaw.admin.application.parking.service.PkParkingService;
import com.cyyaw.admin.application.parking.service.ParkingExitService;
import com.cyyaw.admin.dao.parking.PkCarLogDao;
import com.cyyaw.admin.dao.parking.PkCostRulesDao;
import com.cyyaw.admin.entity.dto.parking.ExitChannelVehicleVO;
import com.cyyaw.admin.entity.dto.parking.ExitOrderVO;
import com.cyyaw.admin.entity.module.or.OrOrder;
import com.cyyaw.admin.entity.module.or.OrOrderDetail;
import com.cyyaw.admin.entity.module.or.OrOrderPay;
import com.cyyaw.admin.entity.module.or.OrOrderStatusLog;
import com.cyyaw.admin.entity.module.parking.PkCarLog;
import com.cyyaw.admin.entity.module.parking.PkChannel;
import com.cyyaw.admin.entity.module.parking.PkCostRules;
import com.cyyaw.admin.entity.module.parking.PkParking;
import com.cyyaw.admin.inf.InfOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * H5 扫码缴费出场流程实现。对应 业务流程_h5.md 的三个接口。
 * <p>
 * 出场识别只登记「待缴费」不开闸（见 CarNumberServiceImpl#carOutParking），
 * 支付完成或费用为 0 时才由 {@link #completeExit} 放行。
 */
@Slf4j
@Service
public class ParkingExitServiceImpl implements ParkingExitService {

    /** 支付倒计时时长（分钟），文档要求「30分钟内完成支付」 */
    private static final int PAY_EXPIRE_MINUTES = 30;
    /** 订单支付状态：已支付 */
    private static final int PAY_STATUS_PAID = 2;
    /** 订单状态：已完成 */
    private static final int ORDER_STATUS_DONE = 4;
    /** 停车记录状态：已出场 */
    private static final int CAR_LOG_STATUS_OUT = 1;
    /** 操作人类型：系统 */
    private static final int OPERATOR_SYSTEM = 1;
    /** 车辆重入导致旧停车记录被强制结束时，写到订单备注上的说明 */
    private static final String STALE_ORDER_REMARK = "车辆重新入场，旧停车记录被强制结束";

    @Autowired
    private PkCarLogService pkCarLogService;

    @Autowired
    private PkCarLogDao pkCarLogDao;

    @Autowired
    private PkParkingService pkParkingService;

    @Autowired
    private PkChannelService pkChannelService;

    @Autowired
    private PkCostRulesDao pkCostRulesDao;

    @Autowired
    private InfOrder infOrder;

    @Autowired
    private IotService iotService;

    @Override
    public ExitChannelVehicleVO findChannelVehicle(Long channelId) {
        if (channelId == null) {
            // 停车场二维码（不带通道）：由车主手动输入车牌
            return null;
        }
        PkCarLog carLog = pkCarLogDao.selectWaitingExitByChannelId(channelId);
        if (carLog == null) {
            return null;
        }
        ExitChannelVehicleVO vo = new ExitChannelVehicleVO();
        vo.setCarNumber(carLog.getCarNumber());
        vo.setCarType(carLog.getCarType());
        vo.setParkingId(carLog.getParkingId());
        vo.setParkingName(parkingName(carLog.getParkingId()));
        vo.setChannelId(channelId);
        PkChannel channel = pkChannelService.findById(channelId);
        if (channel != null) {
            vo.setChannelName(channel.getName());
        }
        vo.setEntryTime(carLog.getEntryTime());
        return vo;
    }

    @Override
    public ExitOrderVO findExitOrder(Long parkingId, String carNumber) {
        if (parkingId == null || carNumber == null || carNumber.isBlank()) {
            return null;
        }
        // 只认「还在场内」的记录
        PkCarLog carLog = pkCarLogService.selectByParkingIdAndCarNumber(parkingId, carNumber.trim());
        if (carLog == null) {
            return null;
        }
        OrOrder order = refreshOrder(carLog);
        if (order == null) {
            return null;
        }
        return buildOrderVO(carLog, order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrOrder refreshOrder(PkCarLog carLog) {
        if (carLog == null || carLog.getId() == null) {
            return null;
        }
        // 规则每次重新取：出场前管理员可能刚改过费率
        List<PkCostRules> rules = pkCostRulesDao.selectByParkingId(carLog.getParkingId());
        LocalDateTime entryTime = carLog.getEntryTime() == null ? LocalDateTime.now() : carLog.getEntryTime();
        BigDecimal amount = CostUtil.computeCost(entryTime, LocalDateTime.now(), carLog.getCarType(), rules);
        return infOrder.updateOrderAmountByCarLogId(carLog.getId(), carLog.getParkingId(), amount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeExit(Long orderId, Integer payType, String payNo) {
        OrOrder order = infOrder.findOrderById(orderId);
        if (order == null) {
            log.warn("出场放行：订单不存在 orderId={}", orderId);
            return;
        }
        if (order.getPayStatus() != null && order.getPayStatus() == PAY_STATUS_PAID) {
            log.info("出场放行：订单已支付，跳过 orderId={}", orderId);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        BigDecimal payAmount = order.getPayAmount() == null ? BigDecimal.ZERO : order.getPayAmount();

        // 1. 订单置已支付
        order.setPayStatus(PAY_STATUS_PAID);
        order.setOrderStatus(ORDER_STATUS_DONE);
        order.setPayAmounted(payAmount);
        order.setPayTime(now);
        infOrder.saveOrder(order);

        // 2. 支付记录
        OrOrderPay pay = new OrOrderPay();
        pay.setOrderId(order.getId());
        pay.setPayNo(payNo);
        pay.setPayType(payType);
        pay.setPayAmount(payAmount);
        pay.setPayStatus(PAY_STATUS_PAID);
        pay.setPayTime(now);
        pay.setCallbackTime(now);
        pay.setCallbackContent("出场缴费完成，支付渠道待接入");
        pay.setEnId(order.getEnId());
        pay.setCreateTime(now);
        pay.setUpdateTime(now);
        pay.setNote("");
        infOrder.saveOrderPay(pay);

        // 3. 状态变更日志
        OrOrderStatusLog statusLog = new OrOrderStatusLog();
        statusLog.setOrderId(order.getId());
        statusLog.setBeforeStatus(0);
        statusLog.setOrderStatus(ORDER_STATUS_DONE);
        statusLog.setRemark("出场缴费完成");
        statusLog.setOperatorType(OPERATOR_SYSTEM);
        statusLog.setEnId(order.getEnId());
        statusLog.setCreateTime(now);
        statusLog.setUpdateTime(now);
        statusLog.setNote("");
        infOrder.saveOrderStatusLog(statusLog);

        // 4. 结束停车记录 + 开闸
        PkCarLog carLog = findCarLogByOrder(order.getId());
        if (carLog == null) {
            log.warn("出场放行：订单 {} 找不到关联的停车记录，仅完成订单状态流转", orderId);
            return;
        }
        carLog.setStatus(CAR_LOG_STATUS_OUT);
        carLog.setOutTime(now);
        pkCarLogService.save(carLog);
        openGate(carLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleStaleOrder(PkCarLog carLog) {
        OrOrder order = refreshOrder(carLog);
        if (order == null) {
            return;
        }
        // 只是标注结束原因，订单仍保持未支付，留给通行记录页的「补费」处理
        order.setRemark(STALE_ORDER_REMARK);
        infOrder.saveOrder(order);
        log.info("旧停车记录 {} 已强制结束，订单 {} 按实际时长结算为 {}（未支付）",
                carLog.getId(), order.getId(), order.getPayAmount());
    }

    /**
     * 按订单明细的 business_id 反查停车记录（business_id 存的就是 pk_car_log.id）。
     */
    private PkCarLog findCarLogByOrder(Long orderId) {
        List<OrOrderDetail> detailList = infOrder.findOrderDetailList(orderId);
        if (detailList == null || detailList.isEmpty()) {
            return null;
        }
        Long carLogId = detailList.get(0).getBusinessId();
        return carLogId == null ? null : pkCarLogDao.selectById(carLogId);
    }

    private void openGate(PkCarLog carLog) {
        String deviceCode = carLog.getOutDeviceCode();
        if (deviceCode == null || deviceCode.isBlank()) {
            log.warn("出场放行：停车记录 {} 没有出场设备编码，无法下发开闸", carLog.getId());
            return;
        }
        iotService.ctlBarrierGate(deviceCode, true);
        iotService.ctlScreen(deviceCode, carLog.getCarNumber() + " 一路顺风");
    }

    private ExitOrderVO buildOrderVO(PkCarLog carLog, OrOrder order) {
        LocalDateTime entryTime = carLog.getEntryTime();
        LocalDateTime now = LocalDateTime.now();
        ExitOrderVO vo = new ExitOrderVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setParkingId(carLog.getParkingId());
        vo.setParkingName(parkingName(carLog.getParkingId()));
        vo.setCarNumber(carLog.getCarNumber());
        vo.setEntryTime(entryTime);
        vo.setDuration(CostUtil.formatDuration(entryTime, now));
        vo.setDurationMinutes(entryTime == null ? 0L : Duration.between(entryTime, now).toMinutes());
        vo.setAmount(order.getPayAmount());
        vo.setPayStatus(order.getPayStatus());
        vo.setExpireTime(now.plusMinutes(PAY_EXPIRE_MINUTES));
        return vo;
    }

    private String parkingName(Long parkingId) {
        if (parkingId == null) {
            return null;
        }
        PkParking parking = pkParkingService.findById(parkingId);
        return parking == null ? null : parking.getName();
    }

}
