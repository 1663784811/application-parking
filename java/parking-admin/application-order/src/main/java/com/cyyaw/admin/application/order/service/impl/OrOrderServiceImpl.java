package com.cyyaw.admin.application.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.order.service.OrOrderService;
import com.cyyaw.admin.dao.or.OrOrderDao;
import com.cyyaw.admin.dao.or.OrOrderDetailDao;
import com.cyyaw.admin.dao.or.OrOrderPayDao;
import com.cyyaw.admin.dao.or.OrOrderStatusLogDao;
import com.cyyaw.admin.entity.module.or.OrOrder;
import com.cyyaw.admin.entity.module.or.OrOrderDetail;
import com.cyyaw.admin.entity.module.or.OrOrderPay;
import com.cyyaw.admin.entity.module.or.OrOrderStatusLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrOrderServiceImpl implements OrOrderService {

    @Autowired
    private OrOrderDao orOrderDao;

    @Autowired
    private OrOrderDetailDao orOrderDetailDao;

    @Autowired
    private OrOrderPayDao orOrderPayDao;

    @Autowired
    private OrOrderStatusLogDao orOrderStatusLogDao;

    @Override
    public Page<OrOrder> findPage(Integer page, Integer size, QueryWrapper<OrOrder> wrapper) {
        // 排序固定按创建时间倒序，保证列表顺序一致
        wrapper.orderByDesc("create_time");
        return orOrderDao.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public OrOrder findByOrderNo(String orderNo) {
        return orOrderDao.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrOrder>()
                        .eq("order_no", orderNo)
        );
    }

    @Override
    public OrOrder findById(Long id) {
        return orOrderDao.selectById(id);
    }

    @Override
    public OrOrder saveOrder(OrOrder order) {
        return orOrderDao.save(order);
    }

    @Override
    public List<OrOrderDetail> findOrderDetailList(Long orderId) {
        return orOrderDetailDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrOrderDetail>()
                        .eq("order_id", orderId)
        );
    }

    @Override
    public OrOrderDetail saveOrderDetail(OrOrderDetail detail) {
        return orOrderDetailDao.save(detail);
    }

    @Override
    public OrOrderPay saveOrderPay(OrOrderPay pay) {
        return orOrderPayDao.save(pay);
    }

    @Override
    public List<OrOrderPay> findOrderPayList(Long orderId) {
        return orOrderPayDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrOrderPay>()
                        .eq("order_id", orderId)
        );
    }

    @Override
    public OrOrderStatusLog saveOrderStatusLog(OrOrderStatusLog log) {
        return orOrderStatusLogDao.save(log);
    }

    @Override
    public List<OrOrderStatusLog> findOrderStatusLogList(Long orderId) {
        return orOrderStatusLogDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrOrderStatusLog>()
                        .eq("order_id", orderId)
        );
    }

}