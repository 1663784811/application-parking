package com.cyyaw.admin.application.parking.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.parking.service.PkParkingService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.module.parking.PkParking;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "停车场管理")
@RestController
@RequestMapping("/admin/parking/parking")
public class PkParkingController {

    @Autowired
    private PkParkingService pkParkingService;

    @Operation(summary = "查询停车场", description = "根据ID查询停车场")
    @GetMapping("/find/{id}")
    public BaseResult<PkParking> findById(@PathVariable Long id) {
        PkParking parking = pkParkingService.findById(id);
        return BaseResult.ok(parking);
    }

    @Operation(summary = "停车场列表", description = "分页查询停车场列表")
    @GetMapping("/list")
    public BaseResult<List<PkParking>> list(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false) Integer status) {
        QueryWrapper<PkParking> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name);
        }
        if (status != null) {
            wrapper.eq("opening_up", status == 1 ? 0 : 1);
        }
        wrapper.orderByDesc("create_time");
        Page<PkParking> pageResult = pkParkingService.findPage(page, size, wrapper);
        BaseResult.Result result = new BaseResult.Result(page, size, pageResult.getTotal());
        return BaseResult.ok(pageResult.getRecords(), result);
    }

    @Operation(summary = "保存停车场", description = "新增或更新停车场")
    @PostMapping("/save")
    public BaseResult<PkParking> save(@RequestBody PkParking parking) {
        PkParking result = pkParkingService.save(parking);
        return BaseResult.ok(result);
    }

    @Operation(summary = "删除停车场", description = "根据ID删除停车场")
    @DeleteMapping("/delete/{id}")
    public BaseResult<Void> delete(@PathVariable Long id) {
        pkParkingService.delete(id);
        return BaseResult.ok();
    }

}