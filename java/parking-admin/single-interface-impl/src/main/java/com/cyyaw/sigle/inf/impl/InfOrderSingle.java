package com.cyyaw.sigle.inf.impl;

import com.cyyaw.admin.application.order.service.OrOrderService;
import com.cyyaw.admin.entity.module.or.OrOrder;
import com.cyyaw.admin.entity.module.or.OrOrderDetail;
import com.cyyaw.admin.inf.InfOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InfOrderSingle implements InfOrder {


    @Autowired
    private OrOrderService orOrderService;


    @Override
    public OrOrder createCarNumberOrder(List<OrOrderDetail> orderDetailList) {
        return orOrderService.createOrder(orderDetailList);
    }
}
