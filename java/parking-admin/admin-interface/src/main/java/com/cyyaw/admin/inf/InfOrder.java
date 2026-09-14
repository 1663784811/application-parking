package com.cyyaw.admin.inf;

import com.cyyaw.admin.entity.module.or.OrOrder;
import com.cyyaw.admin.entity.module.or.OrOrderDetail;

import java.util.List;

public interface InfOrder {


    /**
     * 创建车牌订单
     */
    OrOrder createOrder(List<OrOrderDetail> orderDetailList);


}
