package com.cyyaw.application.parking.gate.contoller;

import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.application.parking.gate.client.AdminCarLogClient;
import com.cyyaw.application.parking.gate.common.entity.VehicleRecord;
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
 * <p>数据来源为 admin 的 /internal/parking/carLog/list 接口（门口端不持有
 * 停车记录数据，统一由 admin 落库与查询）；admin 不可达时降级返回空列表。</p>
 */
@Tag(name = "车辆通行记录")
@RestController
@RequestMapping("/api/vehicle")
@RequiredArgsConstructor
public class VehicleRecordController {

    private final AdminCarLogClient adminCarLogClient;

    /**
     * 返回车辆通行记录列表（最新在前）。
     */
    @Operation(summary = "通行记录列表", description = "返回车辆通行记录，最新在前（需带登录 token）")
    @GetMapping("/records")
    public BaseResult<List<VehicleRecord>> records() {
        return BaseResult.ok(adminCarLogClient.listRecords(1, 10, null));
    }
}
