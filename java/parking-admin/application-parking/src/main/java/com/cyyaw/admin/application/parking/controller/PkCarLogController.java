package com.cyyaw.admin.application.parking.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.parking.service.PkCarLogService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.parking.PkCarFindByCarDTO;
import com.cyyaw.admin.entity.dto.parking.PkCarLogQueryDTO;
import com.cyyaw.admin.entity.dto.parking.PkCarStatusCountDTO;
import com.cyyaw.admin.entity.module.parking.PkCarLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "停车记录管理")
@RestController
@RequestMapping("/admin/parking/carLog")
public class PkCarLogController {

    @Autowired
    private PkCarLogService pkCarLogService;

    @Operation(summary = "查询停车记录", description = "根据ID查询停车记录")
    @GetMapping("/find/{id}")
    public BaseResult<PkCarLog> findById(@PathVariable Long id) {
        PkCarLog carLog = pkCarLogService.findById(id);
        return BaseResult.ok(carLog);
    }

    @Operation(summary = "停车记录列表", description = "分页查询停车记录；以 PkCarLogQueryDTO 接收查询参数")
    @GetMapping("/list")
    public BaseResult<List<PkCarLog>> list(PkCarLogQueryDTO query) {
        Integer page = query.getPage();
        Integer size = query.getSize();
        Long parkingId = query.getParkingId();
        String carNumber = query.getCarNumber();
        QueryWrapper<PkCarLog> wrapper = new QueryWrapper<>();
        if (parkingId != null) {
            wrapper.eq("parking_id", parkingId);
        }
        if (carNumber != null && !carNumber.isEmpty()) {
            wrapper.like("car_number", carNumber);
        }
        if (query.getStatus() != null) {
            wrapper.eq("status", query.getStatus());
        }
        wrapper.orderByDesc("entry_time");
        Page<PkCarLog> pageResult = pkCarLogService.findPage(page, size, wrapper);
        BaseResult.Result result = new BaseResult.Result(page, size, pageResult.getTotal());
        return BaseResult.ok(pageResult.getRecords(), result);
    }

    @Operation(summary = "保存停车记录", description = "新增或更新停车记录")
    @PostMapping("/save")
    public BaseResult<PkCarLog> save(@RequestBody PkCarLog carLog) {
        PkCarLog result = pkCarLogService.save(carLog);
        return BaseResult.ok(result);
    }

    @Operation(summary = "在场车辆数", description = "查询停车场当前在场车辆数；parkingId 为路径参数，status 以 PkCarStatusCountDTO 接收")
    @GetMapping("/statusCount/{parkingId}")
    public BaseResult<Integer> statusCount(@PathVariable Long parkingId, PkCarStatusCountDTO query) {
        int status = query.getStatus() == null ? 0 : query.getStatus();
        int count = pkCarLogService.selectStatusCount(parkingId, status);
        return BaseResult.ok(count);
    }

    @Operation(summary = "查询在场车辆", description = "根据停车场ID和车牌查询在场车辆；以 PkCarFindByCarDTO 接收查询参数")
    @GetMapping("/findByCar")
    public BaseResult<PkCarLog> findByCar(PkCarFindByCarDTO query) {
        PkCarLog carLog = pkCarLogService.selectByParkingIdAndCarNumber(query.getParkingId(), query.getCarNumber());
        return BaseResult.ok(carLog);
    }

    @Operation(summary = "今日通行数", description = "统计今日入场车辆数")
    @GetMapping("/todayCount")
    public BaseResult<Integer> todayCount() {
        int count = pkCarLogService.selectTodayCount();
        return BaseResult.ok(count);
    }

}
