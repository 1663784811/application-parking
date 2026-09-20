package com.cyyaw.admin.application.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.or.OrOrder;
import com.cyyaw.admin.entity.module.or.OrOrderDetail;
import com.cyyaw.admin.entity.module.or.OrOrderPay;
import com.cyyaw.admin.entity.module.or.OrOrderStatusLog;

import java.math.BigDecimal;
import java.util.List;

public interface OrOrderService {

    /**
     * 分页查询订单（条件由调用方构造 QueryWrapper 传入）。
     */
    Page<OrOrder> findPage(Integer page, Integer size, QueryWrapper<OrOrder> wrapper);

    OrOrder findByOrderNo(String orderNo);

    OrOrder findById(Long id);

    OrOrder saveOrder(OrOrder order);

    List<OrOrderDetail> findOrderDetailList(Long orderId);

    OrOrderDetail saveOrderDetail(OrOrderDetail detail);

    OrOrderPay saveOrderPay(OrOrderPay pay);

    List<OrOrderPay> findOrderPayList(Long orderId);

    OrOrderStatusLog saveOrderStatusLog(OrOrderStatusLog log);

    List<OrOrderStatusLog> findOrderStatusLogList(Long orderId);


    OrOrder createOrder(List<OrOrderDetail> orderDetailList);

    /**
     * 按停车记录ID查订单。停车场景下 or_order_detail.business_id 存的是 pk_car_log.id，
     * 这是停车记录与订单之间唯一的关联链路。
     */
    OrOrder findOrderByCarLogId(Long carLogId);

    /**
     * 按停车记录ID更新订单金额（主表与明细同步），用于出场时按实际停车时长重算费用。
     *
     * @param parkingId 停车场ID，写入 or_order.store_id；为空表示不改
     * @return 更新后的订单；找不到订单返回 null
     */
    OrOrder updateOrderAmountByCarLogId(Long carLogId, Long parkingId, BigDecimal amount);

}