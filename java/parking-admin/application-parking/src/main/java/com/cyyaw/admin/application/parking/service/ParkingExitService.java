package com.cyyaw.admin.application.parking.service;

import com.cyyaw.admin.entity.dto.parking.ExitChannelVehicleVO;
import com.cyyaw.admin.entity.dto.parking.ExitOrderVO;
import com.cyyaw.admin.entity.module.or.OrOrder;
import com.cyyaw.admin.entity.module.parking.PkCarLog;

/**
 * H5 扫码缴费出场流程（业务流程_h5.md 的三个接口）。
 */
public interface ParkingExitService {

    /**
     * 接口1：查询某出场通道当前正在等待缴费出场的车辆。
     *
     * @param channelId 通道ID，为空表示扫的是停车场二维码（不带通道），返回 null
     * @return 待出场车辆，没有则返回 null
     */
    ExitChannelVehicleVO findChannelVehicle(Long parkingId, Long channelId);

    /**
     * 接口2：按车牌查询当前停车场待缴的停车费订单。查不到在场记录时返回 null。
     */
    ExitOrderVO findExitOrder(Long parkingId, String carNumber);

    /**
     * 按入场时间重新计算停车费并写回订单（金额与订单详情同步更新）。
     * 出场识别与接口2 共用这一段，避免两处计费口径不一致。
     *
     * @return 更新后的订单；无订单或计费失败返回 null
     */
    OrOrder refreshOrder(PkCarLog carLog);

    /**
     * 缴费完成后放行：订单置已支付、写支付记录与状态日志、结束停车记录、下发开闸。
     * 支付渠道接入前，由 /internal/parking/exit/complete 直接调用，便于端到端联调；
     * 将来真实支付回调复用同一入口。
     *
     * @param payType 支付方式{1:支付宝,2:微信支付,3:银行卡,4:现金}
     * @param payNo   第三方支付流水号，可为空
     */
    void completeExit(Long orderId, Integer payType, String payNo);

    /**
     * 车辆重新入场、旧停车记录被强制结束时调用：按实际时长把旧订单的金额结算出来，
     * 并标注结束原因。订单**保持未支付** —— 通行记录页的「补费」只针对未缴记录，
     * 这里若直接作废会把该收的钱弄丢。
     */
    void settleStaleOrder(PkCarLog carLog);

}
