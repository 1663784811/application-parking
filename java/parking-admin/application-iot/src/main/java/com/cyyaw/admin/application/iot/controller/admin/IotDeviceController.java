package com.cyyaw.admin.application.iot.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.entity.dto.iot.IotDeviceQueryDTO;
import com.cyyaw.admin.application.iot.service.IotDeviceService;
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
public class IotDeviceController {

    @Autowired
    private IotDeviceService iotDeviceService;

    @Operation(summary = "查询设备", description = "根据ID查询设备")
    @GetMapping("/find/{id}")
    public BaseResult<IotDevice> findById(@PathVariable Long id) {
        return BaseResult.ok(iotDeviceService.findById(id));
    }

    @Operation(summary = "设备列表", description = "分页查询设备；查询参数 page/size/type/onlineStatus/keyword，以 IotDeviceQueryDTO 实体类接收（Spring 隐式 @ModelAttribute 按名绑定查询串到字段）")
    @GetMapping("/list")
    public BaseResult<List<IotDevice>> list(IotDeviceQueryDTO query) {
        int page = query.getPage();
        int size = query.getSize();
        String type = query.getType();
        String keyword = query.getKeyword();
        Integer onlineStatus = query.getOnlineStatus();

        QueryWrapper<IotDevice> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(type)) {
            wrapper.eq("type", type);
        }
        if (onlineStatus != null) {
            wrapper.eq("online_status", onlineStatus);
        }
        if (StringUtils.hasText(keyword)) {
            // 设备编号或名称模糊匹配
            wrapper.and(w -> w.like("code", keyword).or().like("name", keyword));
        }
        wrapper.orderByDesc("create_time");
        Page<IotDevice> pageResult = iotDeviceService.findPage(page, size, wrapper);
        BaseResult.Result result = new BaseResult.Result(page, size, pageResult.getTotal());
        return BaseResult.ok(pageResult.getRecords(), result);
    }

    @Operation(summary = "保存设备", description = "新增或更新设备")
    @PostMapping("/save")
    public BaseResult<IotDevice> save(@RequestBody IotDevice device) {
        return BaseResult.ok(iotDeviceService.save(device));
    }

    @Operation(summary = "删除设备", description = "根据ID删除设备")
    @DeleteMapping("/delete/{id}")
    public BaseResult<Void> delete(@PathVariable Long id) {
        iotDeviceService.delete(id);
        return BaseResult.ok();
    }

    @Operation(summary = "远程重启", description = "根据ID远程重启设备")
    @PostMapping("/restart/{id}")
    public BaseResult<Void> restart(@PathVariable Long id) {
        iotDeviceService.restart(id);
        return BaseResult.ok();
    }

    @Operation(summary = "设备统计", description = "设备数量统计")
    @GetMapping("/stats")
    public BaseResult<Map<String, Object>> stats() {
        return BaseResult.ok(iotDeviceService.stats());
    }

}
