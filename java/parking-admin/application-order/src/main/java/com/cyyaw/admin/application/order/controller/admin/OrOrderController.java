package com.cyyaw.admin.application.order.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.order.service.OrOrderService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.order.OrOrderFindByOrderNoDTO;
import com.cyyaw.admin.entity.dto.order.OrOrderQueryDTO;
import com.cyyaw.admin.entity.module.or.OrOrder;
import com.cyyaw.admin.entity.module.or.OrOrderDetail;
import com.cyyaw.admin.entity.module.or.OrOrderPay;
import com.cyyaw.admin.entity.module.or.OrOrderStatusLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单管理")
@RestController
@RequestMapping("/admin/order")
public class OrOrderController {

    @Autowired
    private OrOrderService orOrderService;

    @Operation(summary = "订单列表", description = "分页查询订单列表（支持订单号、订单状态、支付状态、创建时间区间筛选）；以 OrOrderQueryDTO 接收查询参数")
    @GetMapping("/list")
    public BaseResult<List<OrOrder>> list(OrOrderQueryDTO query) {
        Integer page = query.getPage();
        Integer size = query.getSize();
        String orderNo = query.getOrderNo();
        Integer orderStatus = query.getOrderStatus();
        Integer payStatus = query.getPayStatus();
        String startDate = query.getStartDate();
        String endDate = query.getEndDate();
        QueryWrapper<OrOrder> wrapper = new QueryWrapper<>();
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like("order_no", orderNo);
        }
        if (orderStatus != null) {
            wrapper.eq("order_status", orderStatus);
        }
        if (payStatus != null) {
            wrapper.eq("pay_status", payStatus);
        }
        // 创建时间区间（含端点）
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge("create_time", startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le("create_time", endDate + " 23:59:59");
        }
        Page<OrOrder> pageResult = orOrderService.findPage(page, size, wrapper);
        BaseResult.Result result = new BaseResult.Result(page, size, pageResult.getTotal());
        return BaseResult.ok(pageResult.getRecords(), result);
    }

    @Operation(summary = "查询订单", description = "根据订单ID查询订单")
    @GetMapping("/find/{id}")
    public BaseResult<OrOrder> findById(@PathVariable Long id) {
        OrOrder order = orOrderService.findById(id);
        return BaseResult.ok(order);
    }

    @Operation(summary = "查询订单", description = "根据订单编号查询订单；以 OrOrderFindByOrderNoDTO 接收查询参数")
    @GetMapping("/findByOrderNo")
    public BaseResult<OrOrder> findByOrderNo(OrOrderFindByOrderNoDTO query) {
        OrOrder order = orOrderService.findByOrderNo(query.getOrderNo());
        return BaseResult.ok(order);
    }

    @Operation(summary = "保存订单", description = "新增或更新订单")
    @PostMapping("/save")
    public BaseResult<OrOrder> save(@RequestBody OrOrder order) {
        OrOrder result = orOrderService.saveOrder(order);
        return BaseResult.ok(result);
    }

    @Operation(summary = "查询订单明细", description = "根据订单ID查询订单明细列表")
    @GetMapping("/detail/list/{orderId}")
    public BaseResult<List<OrOrderDetail>> detailList(@PathVariable Long orderId) {
        List<OrOrderDetail> list = orOrderService.findOrderDetailList(orderId);
        return BaseResult.ok(list);
    }

    @Operation(summary = "保存订单明细", description = "新增或更新订单明细")
    @PostMapping("/detail/save")
    public BaseResult<OrOrderDetail> saveDetail(@RequestBody OrOrderDetail detail) {
        OrOrderDetail result = orOrderService.saveOrderDetail(detail);
        return BaseResult.ok(result);
    }

    @Operation(summary = "查询支付记录", description = "根据订单ID查询支付记录列表")
    @GetMapping("/pay/list/{orderId}")
    public BaseResult<List<OrOrderPay>> payList(@PathVariable Long orderId) {
        List<OrOrderPay> list = orOrderService.findOrderPayList(orderId);
        return BaseResult.ok(list);
    }

    @Operation(summary = "保存支付记录", description = "新增支付记录")
    @PostMapping("/pay/save")
    public BaseResult<OrOrderPay> savePay(@RequestBody OrOrderPay pay) {
        OrOrderPay result = orOrderService.saveOrderPay(pay);
        return BaseResult.ok(result);
    }

    @Operation(summary = "查询状态日志", description = "根据订单ID查询状态变更日志")
    @GetMapping("/statusLog/list/{orderId}")
    public BaseResult<List<OrOrderStatusLog>> statusLogList(@PathVariable Long orderId) {
        List<OrOrderStatusLog> list = orOrderService.findOrderStatusLogList(orderId);
        return BaseResult.ok(list);
    }
}
