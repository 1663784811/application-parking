package com.cyyaw.admin.application.order.controller.admin;

import com.cyyaw.admin.application.order.service.OrCouponService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.module.or.OrCoupon;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "优惠券管理")
@RestController
@RequestMapping("/admin/charge/coupon")
public class OrCouponController {

    @Autowired
    private OrCouponService orCouponService;

    @Operation(summary = "查询优惠券", description = "根据ID查询优惠券")
    @GetMapping("/find/{id}")
    public BaseResult<OrCoupon> findById(@PathVariable Long id) {
        return BaseResult.ok(orCouponService.findById(id));
    }

    @Operation(summary = "优惠券列表", description = "查询全部优惠券（按创建时间倒序）")
    @GetMapping("/list")
    public BaseResult<List<OrCoupon>> list() {
        return BaseResult.ok(orCouponService.findAll());
    }

    @Operation(summary = "保存优惠券", description = "新增或更新优惠券")
    @PostMapping("/save")
    public BaseResult<OrCoupon> save(@RequestBody OrCoupon coupon) {
        return BaseResult.ok(orCouponService.save(coupon));
    }

    @Operation(summary = "删除优惠券", description = "根据ID删除优惠券")
    @DeleteMapping("/delete/{id}")
    public BaseResult<Void> delete(@PathVariable Long id) {
        orCouponService.delete(id);
        return BaseResult.ok();
    }
}
