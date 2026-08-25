package com.cyyaw.admin.application.order.service;

import com.cyyaw.admin.entity.module.or.OrCoupon;

import java.util.List;

public interface OrCouponService {

    OrCoupon findById(Long id);

    OrCoupon save(OrCoupon coupon);

    void delete(Long id);

    List<OrCoupon> findAll();
}
