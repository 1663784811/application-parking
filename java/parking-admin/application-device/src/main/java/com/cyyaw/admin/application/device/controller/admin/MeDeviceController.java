package com.cyyaw.admin.application.device.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.device.service.MeDeviceService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.module.iot.IotDevice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "设备管理")
@RestController
@RequestMapping("/admin/device")
public class MeDeviceController {

    @Autowired
    private MeDeviceService meDeviceService;

    @Operation(summary = "查询设备", description = "根据ID查询设备")
    @GetMapping("/find/{id}")
    public BaseResult<IotDevice> findById(@PathVariable Long id) {
        return BaseResult.ok(meDeviceService.findById(id));
    }

    @Operation(summary = "设备列表", description = "分页查询设备")
    @GetMapping("/list")
    public BaseResult<List<IotDevice>> list(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) String type, @RequestParam(required = false) Long parkingId, @RequestParam(required = false) Integer onlineStatus, @RequestParam(required = false) String keyword) {
        QueryWrapper<IotDevice> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(type)) {
            wrapper.eq("type", type);
        }
        if (parkingId != null) {
            wrapper.eq("parking_id", parkingId);
        }
        if (onlineStatus != null) {
            wrapper.eq("online_status", onlineStatus);
        }
        if (StringUtils.hasText(keyword)) {
            // 设备编号或名称模糊匹配
            wrapper.and(w -> w.like("code", keyword).or().like("name", keyword));
        }
        wrapper.orderByDesc("create_time");
        Page<IotDevice> pageResult = meDeviceService.findPage(page, size, wrapper);
        BaseResult.Result result = new BaseResult.Result(page, size, pageResult.getTotal());
        return BaseResult.ok(pageResult.getRecords(), result);
    }

    @Operation(summary = "保存设备", description = "新增或更新设备")
    @PostMapping("/save")
    public BaseResult<IotDevice> save(@RequestBody IotDevice device) {
        return BaseResult.ok(meDeviceService.save(device));
    }

    @Operation(summary = "删除设备", description = "根据ID删除设备")
    @DeleteMapping("/delete/{id}")
    public BaseResult<Void> delete(@PathVariable Long id) {
        meDeviceService.delete(id);
        return BaseResult.ok();
    }

    @Operation(summary = "远程重启", description = "根据ID远程重启设备")
    @PostMapping("/restart/{id}")
    public BaseResult<Void> restart(@PathVariable Long id) {
        meDeviceService.restart(id);
        return BaseResult.ok();
    }

    @Operation(summary = "设备统计", description = "设备数量统计")
    @GetMapping("/stats")
    public BaseResult<Map<String, Object>> stats() {
        return BaseResult.ok(meDeviceService.stats());
    }

}
