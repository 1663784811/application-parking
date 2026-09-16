package com.cyyaw.admin.application.parking.controller;

import com.cyyaw.admin.application.parking.service.PkSpaceService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.parking.PkSpaceQueryDTO;
import com.cyyaw.admin.entity.module.parking.PkSpace;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "车位管理")
@RestController
@RequestMapping("/admin/parking/space")
public class PkSpaceController {

    @Autowired
    private PkSpaceService pkSpaceService;

    @Operation(summary = "车位列表", description = "按停车场/状态/类型/编号筛选，返回全部匹配车位（平面图）；以 PkSpaceQueryDTO 接收查询参数")
    @GetMapping("/list")
    public BaseResult<List<PkSpace>> list(PkSpaceQueryDTO query) {
        return BaseResult.ok(pkSpaceService.list(query.getParkingId(), query.getStatus(), query.getType(), query.getKeyword()));
    }

    @Operation(summary = "车位状态统计", description = "返回 { free, fixed, temp, fault }")
    @GetMapping("/stats/{parkingId}")
    public BaseResult<Map<String, Object>> stats(@PathVariable Long parkingId) {
        return BaseResult.ok(pkSpaceService.stats(parkingId));
    }

    @Operation(summary = "车位详情")
    @GetMapping("/find/{id}")
    public BaseResult<PkSpace> findById(@PathVariable Long id) {
        return BaseResult.ok(pkSpaceService.findById(id));
    }

    @Operation(summary = "保存车位", description = "新增或更新车位")
    @PostMapping("/save")
    public BaseResult<PkSpace> save(@RequestBody PkSpace space) {
        return BaseResult.ok(pkSpaceService.save(space));
    }

    @Operation(summary = "分配车位", description = "绑定会员/车牌/有效期，状态置为占用")
    @PostMapping("/assign")
    public BaseResult<PkSpace> assign(@RequestBody PkSpace input) {
        return BaseResult.ok(pkSpaceService.assign(input));
    }

    @Operation(summary = "解绑车位", description = "清除绑定，状态置为空闲")
    @PostMapping("/unbind/{id}")
    public BaseResult<Void> unbind(@PathVariable Long id) {
        pkSpaceService.unbind(id);
        return BaseResult.ok();
    }

    @Operation(summary = "车位报修", description = "状态置为故障")
    @PostMapping("/repair/{id}")
    public BaseResult<Void> repair(@PathVariable Long id) {
        pkSpaceService.repair(id);
        return BaseResult.ok();
    }

    @Operation(summary = "删除车位")
    @DeleteMapping("/delete/{id}")
    public BaseResult<Void> delete(@PathVariable Long id) {
        pkSpaceService.delete(id);
        return BaseResult.ok();
    }

}
