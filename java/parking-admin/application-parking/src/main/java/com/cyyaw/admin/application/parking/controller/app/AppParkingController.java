package com.cyyaw.admin.application.parking.controller.app;

import com.cyyaw.admin.application.parking.service.AppParkingService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.parking.AppParkingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * H5 首页「附近停车场」接口。
 * <p>
 * 路径不在 SecurityConfig 白名单内，需要 H5 登录后的 JWT
 * （首页在登录后才可达，axios 拦截器会带上 token）。
 * 剩余车位与在场看板同一口径，见 {@code PkCarLogService#countInLot}。
 */
@Tag(name = "APP-附近停车场")
@RestController
@RequestMapping("/app/parking")
@RequiredArgsConstructor
public class AppParkingController {

    private final AppParkingService appParkingService;

    @Operation(summary = "附近停车场列表", description = "对外开放的停车场；传了 lng/lat 则按距离升序返回距离，未传则按创建时间倒序且无距离")
    @GetMapping("/list")
    public BaseResult<List<AppParkingVO>> list(Long appId, Double lng, Double lat) {
        return BaseResult.ok(appParkingService.findList(appId, lng, lat));
    }

}