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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrOrder createOrder(List<OrOrderDetail> orderDetailList) {
        if (orderDetailList == null || orderDetailList.isEmpty()) {
            return null;
        }
        // 汇总订单详情金额：总金额、优惠金额，实付 = 总金额 - 优惠金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        for (OrOrderDetail detail : orderDetailList) {
            BigDecimal detailTotal = detail.getTotalAmount() == null ? BigDecimal.ZERO : detail.getTotalAmount();
            BigDecimal detailDiscount = detail.getDiscountAmount() == null ? BigDecimal.ZERO : detail.getDiscountAmount();
            totalAmount = totalAmount.add(detailTotal);
            discountAmount = discountAmount.add(detailDiscount);
        }
        BigDecimal payAmount = totalAmount.subtract(discountAmount);

        // 构建主订单
        OrOrder order = new OrOrder();
        // order_no 有唯一键 uk_order_no，补 4 位随机避免同毫秒并发冲突
        order.setOrderNo("O" + System.currentTimeMillis() + (int) (Math.random() * 9000 + 1000));
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(payAmount);
        order.setPayAmounted(BigDecimal.ZERO);
        order.setPayStatus(0);     // 未支付
        order.setOrderStatus(0);   // 待付款
        // 车牌识别流程无登录上下文，MetaObjectHandler 不会回填 enId，这里从订单详情取企业ID
        for (OrOrderDetail detail : orderDetailList) {
            if (detail.getEnId() != null) {
                order.setEnId(detail.getEnId());
                break;
            }
        }
        order = orOrderDao.save(order);
        if (order == null || order.getId() == null) {
            return null;
        }
        // 保存订单详情，回填 orderId
        for (OrOrderDetail detail : orderDetailList) {
            detail.setOrderId(order.getId());
            orOrderDetailDao.save(detail);
        }
        order.setOrderDetailList(orderDetailList);
        // 记录订单初始状态日志（变更前无状态）
        OrOrderStatusLog statusLog = new OrOrderStatusLog();
        statusLog.setOrderId(order.getId());
        statusLog.setBeforeStatus(null);
        statusLog.setOrderStatus(0);
        statusLog.setRemark("创建订单");
        statusLog.setOperatorType(1);   // 系统
        statusLog.setEnId(order.getEnId());
        orOrderStatusLogDao.save(statusLog);
        return order;
    }

}