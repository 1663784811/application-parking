package com.cyyaw.admin.application.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.module.or.OrOrder;
import com.cyyaw.admin.entity.module.or.OrOrderDetail;
import com.cyyaw.admin.entity.module.or.OrOrderPay;
import com.cyyaw.admin.entity.module.or.OrOrderStatusLog;

import java.util.List;

public interface OrOrderService {

    Page<OrOrder> findPage(Integer page, Integer size);

    OrOrder findByOrderNo(String orderNo);

    OrOrder findById(Long id);

    OrOrder saveOrder(OrOrder order);

    List<OrOrderDetail> findOrderDetailList(Long orderId);

    OrOrderDetail saveOrderDetail(OrOrderDetail detail);

    OrOrderPay saveOrderPay(OrOrderPay pay);

    List<OrOrderPay> findOrderPayList(Long orderId);

    OrOrderStatusLog saveOrderStatusLog(OrOrderStatusLog log);

    List<OrOrderStatusLog> findOrderStatusLogList(Long orderId);

}