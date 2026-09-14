package com.cyyaw.admin.application.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.or.OrOrder;
import com.cyyaw.admin.entity.module.or.OrOrderDetail;
import com.cyyaw.admin.entity.module.or.OrOrderPay;
import com.cyyaw.admin.entity.module.or.OrOrderStatusLog;

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


}