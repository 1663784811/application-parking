package com.cyyaw.sigle.inf.impl;

import com.cyyaw.admin.application.order.service.OrOrderService;
import com.cyyaw.admin.entity.module.or.OrOrder;
import com.cyyaw.admin.entity.module.or.OrOrderDetail;
import com.cyyaw.admin.entity.module.or.OrOrderPay;
import com.cyyaw.admin.entity.module.or.OrOrderStatusLog;
import com.cyyaw.admin.inf.InfOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InfOrderSingle implements InfOrder {


    @Autowired
    private OrOrderService orOrderService;


    @Override
    public OrOrder createOrder(List<OrOrderDetail> orderDetailList) {
        return orOrderService.createOrder(orderDetailList);
    }

    @Override
    public OrOrder findOrderById(Long id) {
        return orOrderService.findById(id);
    }

    @Override
    public OrOrder findOrderByCarLogId(Long carLogId) {
        return orOrderService.findOrderByCarLogId(carLogId);
    }

    @Override
    public OrOrder updateOrderAmountByCarLogId(Long carLogId, Long parkingId, BigDecimal amount) {
        return orOrderService.updateOrderAmountByCarLogId(carLogId, parkingId, amount);
    }

    @Override
    public List<OrOrderDetail> findOrderDetailList(Long orderId) {
        return orOrderService.findOrderDetailList(orderId);
    }

    @Override
    public OrOrder saveOrder(OrOrder order) {
        return orOrderService.saveOrder(order);
    }

    @Override
    public OrOrderPay saveOrderPay(OrOrderPay pay) {
        return orOrderService.saveOrderPay(pay);
    }

    @Override
    public OrOrderStatusLog saveOrderStatusLog(OrOrderStatusLog log) {
        return orOrderService.saveOrderStatusLog(log);
    }
}
