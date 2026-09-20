package com.cyyaw.admin.application.parking.controller.internal;

import com.cyyaw.admin.application.parking.service.ParkingExitService;
import com.cyyaw.admin.common.BaseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 出场放行内部接口。
 * <p>
 * 支付渠道尚未接入，本接口把「缴费完成后放行」这段逻辑（订单置已付 + 支付记录 +
 * 状态日志 + 结束停车记录 + 下发开闸）单独暴露出来，供联调与将来的真实支付回调复用。
 * 接入支付宝/微信后，回调验签通过应调用同一个 {@link ParkingExitService#completeExit}。
 * <p>
 * 路径 /internal/** 在 SecurityConfig 中放行（仅供内部服务调用，
 * 生产环境建议绑定 loopback 或加共享密钥）。
 */
@Slf4j
@RestController
@RequestMapping("/internal/parking/exit")
@RequiredArgsConstructor
public class InternalParkingExitController {

    private final ParkingExitService parkingExitService;

    @PostMapping("/complete")
    public BaseResult<Object> complete(@RequestParam Long orderId,
                                       @RequestParam(required = false) Integer payType,
                                       @RequestParam(required = false) String payNo) {
        log.info("出场放行请求：orderId={}, payType={}, payNo={}", orderId, payType, payNo);
        parkingExitService.completeExit(orderId, payType, payNo);
        return BaseResult.ok();
    }

}
