package com.cyyaw.application.parking.gate.vehicle;

import com.cyyaw.application.parking.gate.common.R;
import com.cyyaw.application.parking.gate.vehicle.model.VehicleRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 车辆通行记录接口。
 * <ul>
 *   <li>GET /api/vehicle/records —— 返回车辆通行记录列表（需带登录 token）</li>
 * </ul>
 * <p>数据来源为内存仓库 {@link VehicleRecordStore}，由车牌识别回调实时写入。</p>
 */
@Tag(name = "车辆通行记录")
@RestController
@RequestMapping("/api/vehicle")
@RequiredArgsConstructor
public class VehicleRecordController {

    private final VehicleRecordStore store;

    /**
     * 返回车辆通行记录列表（最新在前）。
     */
    @Operation(summary = "通行记录列表", description = "返回车辆通行记录，最新在前（需带登录 token）")
    @GetMapping("/records")
    public R<List<VehicleRecord>> records() {
        return R.ok(store.list());
    }
}
