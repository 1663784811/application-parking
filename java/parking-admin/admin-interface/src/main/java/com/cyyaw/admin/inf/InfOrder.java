package com.cyyaw.admin.inf;

import com.cyyaw.admin.entity.module.or.OrOrder;
import com.cyyaw.admin.entity.module.or.OrOrderDetail;
import com.cyyaw.admin.entity.module.or.OrOrderPay;
import com.cyyaw.admin.entity.module.or.OrOrderStatusLog;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单模块对停车模块暴露的接口。
 * <p>
 * application-parking 只依赖 admin-interface，不依赖 application-order，
 * 所有跨模块的订单读写都要走这里。
 */
public interface InfOrder {


    /**
     * 创建车牌订单
     */
    OrOrder createOrder(List<OrOrderDetail> orderDetailList);

    /**
     * 按主键查订单
     */
    OrOrder findOrderById(Long id);

    /**
     * 按停车记录ID查订单（business_id = pk_car_log.id）
     */
    OrOrder findOrderByCarLogId(Long carLogId);

    /**
     * 按停车记录ID更新订单金额（订单主表与明细同步），
     * 用于出场时按实际停车时长重算费用。
     *
     * @param parkingId 停车场ID，会写入 or_order.store_id（报表按它关联停车场），可为空表示不改
     * @return 更新后的订单；找不到订单返回 null
     */
    OrOrder updateOrderAmountByCarLogId(Long carLogId, Long parkingId, BigDecimal amount);

    /**
     * 查订单明细列表
     */
    List<OrOrderDetail> findOrderDetailList(Long orderId);

    /**
     * 保存订单，有 id 则更新、无 id 则新增
     */
    OrOrder saveOrder(OrOrder order);

    /**
     * 保存支付记录
     */
    OrOrderPay saveOrderPay(OrOrderPay pay);

    /**
     * 保存订单状态变更日志
     */
    OrOrderStatusLog saveOrderStatusLog(OrOrderStatusLog log);

}
