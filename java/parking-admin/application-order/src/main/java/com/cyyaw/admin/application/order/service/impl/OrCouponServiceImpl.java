package com.cyyaw.admin.application.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyyaw.admin.application.order.service.OrCouponService;
import com.cyyaw.admin.dao.or.OrCouponDao;
import com.cyyaw.admin.entity.module.or.OrCoupon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrCouponServiceImpl implements OrCouponService {

    @Autowired
    private OrCouponDao orCouponDao;

    @Override
    public OrCoupon findById(Long id) {
        return orCouponDao.selectById(id);
    }

    @Override
    public OrCoupon save(OrCoupon coupon) {
        return orCouponDao.save(coupon);
    }

    @Override
    public void delete(Long id) {
        orCouponDao.deleteById(id);
    }

    @Override
    public List<OrCoupon> findAll() {
        return orCouponDao.selectList(new QueryWrapper<OrCoupon>().orderByDesc("create_time"));
    }
}
