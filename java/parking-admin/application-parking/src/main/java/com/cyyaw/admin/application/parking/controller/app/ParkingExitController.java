package com.cyyaw.admin.application.parking.controller.app;

import com.cyyaw.admin.application.parking.service.ParkingExitService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.parking.ExitChannelVehicleVO;
import com.cyyaw.admin.entity.dto.parking.ExitOrderVO;
import com.cyyaw.admin.entity.dto.parking.ExitPayDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * H5 扫码缴费出场接口（业务流程_h5.md）。
 * <p>
 * 页面 /app/{appId}/scanExit?parkingId=&channelId= 打开后：
 * ① 页面加载调 {@code /channelVehicle} 带出该通道正在等待出场的车辆（只认 channelId）；
 * ② 车牌输入完整后调 {@code /order} 查费用；
 * ③ 确认支付调 {@code /pay}。
 * <p>
 * 路径不在 SecurityConfig 白名单内，因此需要 H5 登录后的 JWT。
 */
@Tag(name = "APP-停车出场缴费")
@RestController
@RequestMapping("/app/parking/exit")
@RequiredArgsConstructor
public class ParkingExitController {

    private final ParkingExitService parkingExitService;

    @Operation(summary = "查询通道当前要出场的车辆", description = "页面加载时调用；只需 channelId，为空（停车场二维码）时返回空数据")
    @GetMapping("/channelVehicle")
    public BaseResult<ExitChannelVehicleVO> channelVehicle(Long channelId) {
        return BaseResult.ok(parkingExitService.findChannelVehicle(channelId));
    }

    @Operation(summary = "查询停车费用订单", description = "车牌输入完整时调用；按当前时间实时计费")
    @GetMapping("/order")
    public BaseResult<ExitOrderVO> order(Long parkingId, String carNumber) {
        ExitOrderVO vo = parkingExitService.findExitOrder(parkingId, carNumber);
        if (vo == null) {
            return BaseResult.fail("未查到该车牌的在场停车记录");
        }
        return BaseResult.ok(vo);
    }

    @Operation(summary = "支付停车费用", description = "支付宝、微信；支付渠道尚未接入，本次返回未实现")
    @PostMapping("/pay")
    public BaseResult<Object> pay(@RequestBody ExitPayDTO payDTO) {
        // 支付渠道（支付宝/微信统一下单 + 回调验签）尚未接入。
        // 接入后此处应调用渠道下单并把结果返回给 H5；
        // 支付成功回调再调 ParkingExitService#completeExit 放行。
        return BaseResult.fail("支付通道未接入");
    }

}
